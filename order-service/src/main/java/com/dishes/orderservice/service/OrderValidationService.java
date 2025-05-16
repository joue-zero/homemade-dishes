package com.dishes.orderservice.service;

import com.dishes.orderservice.config.OrderValidationConfig;
import com.dishes.orderservice.dto.DishDTO;
import com.dishes.orderservice.dto.OrderValidationMessage;
import com.dishes.orderservice.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderValidationService {
    private static final Logger logger = LoggerFactory.getLogger(OrderValidationService.class);
    private static final String DISH_SERVICE_URL = "http://localhost:8082/api/dishes";
    private static final String USER_SERVICE_URL = "http://localhost:8081/api/users";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Lazy
    @Autowired
    private OrderService orderService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${order.minimum.charge:10.0}")
    private BigDecimal minimumOrderCharge;

    /**
     * Starts the order validation process by sending a message to check stock availability
     * @param order The order to validate
     */
    public void startOrderValidation(Order order) {
        try {
            logger.info("Starting order validation for order ID: {}", order.getId());
            OrderValidationMessage message = OrderValidationMessage.fromOrder(order);
            
            if (rabbitTemplate != null) {
                logger.info("💤 DELAY: Waiting 2 seconds before sending to stock check queue...");
                rabbitTemplate.convertAndSend(
                    OrderValidationConfig.ORDER_VALIDATION_EXCHANGE,
                    OrderValidationConfig.STOCK_CHECK_ROUTING_KEY,
                    message
                );
            } else {
                logger.warn("RabbitTemplate is null, skipping message send. Processing order locally...");
                // Process order directly without using message queue
                validateAndCompleteOrder(order);
            }
        } catch (Exception e) {
            logger.error("Error sending order validation message: {}", e.getMessage(), e);
            // Process order directly in case of error
            validateAndCompleteOrder(order);
        }
    }

    /**
     * Direct validation and completion of order without using message queue
     */
    private void validateAndCompleteOrder(Order order) {
        logger.info("Processing order {} directly without message queue", order.getId());
        
        // Validate user balance first
        try {
            Long customerId = order.getCustomerId();
            BigDecimal totalAmount = order.getTotalAmount();
            
            // Check if order amount meets minimum charge
            if (totalAmount.compareTo(minimumOrderCharge) < 0) {
                logger.error("Order amount ${} is below minimum charge of ${}", totalAmount, minimumOrderCharge);
                order.setStatus(Order.OrderStatus.REJECTED);
                order.setPaymentStatus(Order.PaymentStatus.FAILED);
                orderService.createOrder(order);
                return;
            }
            
            // Check user balance
            String balanceUrl = USER_SERVICE_URL + "/" + customerId + "/balance";
            BigDecimal userBalance = restTemplate.getForObject(balanceUrl, BigDecimal.class);
            
            if (userBalance == null) {
                logger.error("Could not retrieve balance for user ID: {}", customerId);
                order.setStatus(Order.OrderStatus.REJECTED);
                order.setPaymentStatus(Order.PaymentStatus.FAILED);
                orderService.createOrder(order);
                return;
            }
            
            if (userBalance.compareTo(totalAmount) < 0) {
                logger.error("Insufficient balance: User {} has ${} but order requires ${}", 
                    customerId, userBalance, totalAmount);
                order.setStatus(Order.OrderStatus.REJECTED);
                order.setPaymentStatus(Order.PaymentStatus.FAILED);
                orderService.createOrder(order);
                return;
            }
            
            logger.info("User {} has sufficient balance: ${} for order amount: ${}", 
                customerId, userBalance, totalAmount);
            
            // Update the user's balance
            String balanceUpdateUrl = USER_SERVICE_URL + "/" + customerId + "/balance";
            
            // Create payload for balance update
            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", totalAmount);
            
            // Call user service to update balance
            restTemplate.postForObject(balanceUpdateUrl, payload, BigDecimal.class);
            logger.info("Updated user balance for customer ID: {}, deducted amount: {}", customerId, totalAmount);
            
            // Mark order as completed
            order.setStatus(Order.OrderStatus.COMPLETED);
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            orderService.createOrder(order);
            
            logger.info("Order ID: {} completed successfully via direct processing", order.getId());
        } catch (Exception e) {
            logger.error("Error processing order directly: {}", e.getMessage(), e);
            // Mark order as rejected if there's an error
            order.setStatus(Order.OrderStatus.REJECTED);
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
            orderService.createOrder(order);
        }
    }

    /**
     * Checks if all items in the order are available in stock
     * @param message The order validation message
     */
    @Transactional
    public void checkStock(OrderValidationMessage message) {
        logger.info("Checking stock for order ID: {}", message.getOrderId());
        boolean allItemsInStock = true;
        StringBuilder validationMessageBuilder = new StringBuilder();
        
        for (OrderValidationMessage.OrderItemInfo item : message.getItems()) {
            try {
                String dishServiceUrl = DISH_SERVICE_URL + "/" + item.getDishId();
                DishDTO dish = restTemplate.getForObject(dishServiceUrl, DishDTO.class);
                
                if (dish == null) {
                    logger.error("Dish not found: {}", item.getDishId());
                    allItemsInStock = false;
                    validationMessageBuilder.append("Dish not found: ").append(item.getDishId()).append("; ");
                    continue;
                }
                
                if (!dish.isAvailable()) {
                    logger.error("Dish not available: {}", dish.getName());
                    allItemsInStock = false;
                    validationMessageBuilder.append("Dish not available: ").append(dish.getName()).append("; ");
                    continue;
                }
                
                if (dish.getQuantity() == null || dish.getQuantity() < item.getQuantity()) {
                    logger.error("Insufficient stock for dish: {}. Requested: {}, Available: {}", 
                        dish.getName(), item.getQuantity(), dish.getQuantity());
                    allItemsInStock = false;
                    validationMessageBuilder.append("Insufficient stock for dish: ").append(dish.getName())
                        .append(". Requested: ").append(item.getQuantity())
                        .append(", Available: ").append(dish.getQuantity()).append("; ");
                }
            } catch (Exception e) {
                logger.error("Error checking stock for dish: {}", item.getDishId(), e);
                allItemsInStock = false;
                validationMessageBuilder.append("Error checking dish: ").append(item.getDishId())
                    .append(" - ").append(e.getMessage()).append("; ");
            }
        }
        
        message.setStockAvailable(allItemsInStock);
        message.setValidationMessage(validationMessageBuilder.toString());
        
        // Add a delay before sending the message to the next queue
        logger.info("💤 DELAY: Waiting 2 seconds before sending to next queue...");
        
        if (allItemsInStock) {
            logger.info("Stock check passed for order ID: {}, proceeding to payment validation", message.getOrderId());
            rabbitTemplate.convertAndSend(
                OrderValidationConfig.ORDER_VALIDATION_EXCHANGE,
                OrderValidationConfig.PAYMENT_VALIDATION_ROUTING_KEY,
                message
            );
        } else {
            logger.info("Stock check failed for order ID: {}, rejecting order", message.getOrderId());
            rabbitTemplate.convertAndSend(
                OrderValidationConfig.ORDER_VALIDATION_EXCHANGE,
                OrderValidationConfig.ORDER_REJECTION_ROUTING_KEY,
                message
            );
        }
    }

    /**
     * Validates the payment for the order, including minimum charge check
     * @param message The order validation message
     */
    @Transactional
    public void validatePayment(OrderValidationMessage message) {
        logger.info("Validating payment for order ID: {}", message.getOrderId());
        boolean paymentValid = true;
        StringBuilder validationMessageBuilder = new StringBuilder();
        
        // Check minimum order amount
        if (message.getTotalAmount().compareTo(minimumOrderCharge) < 0) {
            paymentValid = false;
            validationMessageBuilder.append("Order amount $")
                .append(message.getTotalAmount())
                .append(" is below minimum charge of $")
                .append(minimumOrderCharge)
                .append("; ");
            logger.info("Order amount ${} is below minimum charge of ${}", 
                message.getTotalAmount(), minimumOrderCharge);
        }
        
        // Check if user has sufficient balance
        try {
            Order order = orderService.getOrder(message.getOrderId());
            Long customerId = order.getCustomerId();
            BigDecimal orderAmount = order.getTotalAmount();
            
            // Get current user balance
            String balanceUrl = USER_SERVICE_URL + "/" + customerId + "/balance";
            BigDecimal userBalance = restTemplate.getForObject(balanceUrl, BigDecimal.class);
            
            if (userBalance == null) {
                logger.error("Could not retrieve balance for user ID: {}", customerId);
                paymentValid = false;
                validationMessageBuilder.append("Could not validate user balance; ");
            } else if (userBalance.compareTo(orderAmount) < 0) {
                logger.error("Insufficient balance: User {} has ${} but order requires ${}", 
                    customerId, userBalance, orderAmount);
                paymentValid = false;
                validationMessageBuilder.append("Insufficient balance: You have $")
                    .append(userBalance)
                    .append(" but the order requires $")
                    .append(orderAmount)
                    .append("; ");
            } else {
                logger.info("User {} has sufficient balance: ${} for order amount: ${}", 
                    customerId, userBalance, orderAmount);
            }
        } catch (Exception e) {
            logger.error("Error checking user balance: {}", e.getMessage(), e);
            paymentValid = false;
            validationMessageBuilder.append("Error checking user balance: ")
                .append(e.getMessage())
                .append("; ");
        }
        
        // Additional payment validations can be added here
        
        message.setPaymentValidated(paymentValid);
        if (!message.getValidationMessage().isEmpty()) {
            validationMessageBuilder.append(message.getValidationMessage());
        }
        message.setValidationMessage(validationMessageBuilder.toString());
        
        String routingKey = paymentValid ? 
            OrderValidationConfig.ORDER_COMPLETION_ROUTING_KEY : 
            OrderValidationConfig.ORDER_REJECTION_ROUTING_KEY;
            
        logger.info("Payment validation for order ID: {} - Valid: {}", message.getOrderId(), paymentValid);
        
        // Add a delay before sending the message to the next queue
        logger.info("💤 DELAY: Waiting 2 seconds before sending to next queue...");
        
        rabbitTemplate.convertAndSend(
            OrderValidationConfig.ORDER_VALIDATION_EXCHANGE,
            routingKey,
            message
        );
    }

    /**
     * Completes the order after all validations have passed
     * @param message The order validation message
     */
    @Transactional
    public void completeOrder(OrderValidationMessage message) {
        logger.info("Completing order ID: {}", message.getOrderId());
        
        // Update order status
        Order order = orderService.getOrder(message.getOrderId());
        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        orderService.createOrder(order); // Save updated order
        
        // Update user balance
        try {
            Long customerId = order.getCustomerId();
            BigDecimal totalAmount = order.getTotalAmount();
            
            String balanceUrl = USER_SERVICE_URL + "/" + customerId + "/balance";
            
            // Create payload for balance update
            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", totalAmount);
            
            // Call user service to update balance
            restTemplate.postForObject(balanceUrl, payload, BigDecimal.class);
            logger.info("Updated user balance for customer ID: {}, deducted amount: {}", customerId, totalAmount);
        } catch (Exception e) {
            logger.error("Error updating user balance: {}", e.getMessage(), e);
        }
        
        // Update inventory by reducing quantities
        for (OrderValidationMessage.OrderItemInfo item : message.getItems()) {
            try {
                // Get current dish
                String dishServiceUrl = DISH_SERVICE_URL + "/" + item.getDishId();
                DishDTO dish = restTemplate.getForObject(dishServiceUrl, DishDTO.class);
                
                if (dish != null && dish.getQuantity() != null) {
                    // Update dish quantity
                    int newQuantity = dish.getQuantity() - item.getQuantity();
                    dish.setQuantity(newQuantity);
                    
                    // If new quantity is 0, mark as unavailable
                    if (newQuantity <= 0) {
                        dish.setAvailable(false);
                    }
                    
                    // Update dish in dish service
                    restTemplate.put(dishServiceUrl, dish);
                    logger.info("Updated inventory for dish: {}, new quantity: {}", dish.getName(), newQuantity);
                }
            } catch (Exception e) {
                logger.error("Error updating inventory for dish: {}", item.getDishId(), e);
            }
        }
        
        logger.info("Order ID: {} completed successfully", message.getOrderId());
    }

    /**
     * Rejects the order and rolls back any changes
     * @param message The order validation message
     */
    @Transactional
    public void rejectOrder(OrderValidationMessage message) {
        logger.info("Rejecting order ID: {}", message.getOrderId());
        
        // Update order status
        Order order = orderService.getOrder(message.getOrderId());
        order.setStatus(Order.OrderStatus.REJECTED);
        
        if (!message.isPaymentValidated()) {
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
        }
        
        orderService.createOrder(order); // Save updated order
        
        logger.info("Order ID: {} rejected with reason: {}", message.getOrderId(), message.getValidationMessage());
    }
} 