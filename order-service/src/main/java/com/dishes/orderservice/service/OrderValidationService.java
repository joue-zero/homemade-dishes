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

@Service
public class OrderValidationService {
    private static final Logger logger = LoggerFactory.getLogger(OrderValidationService.class);
    private static final String DISH_SERVICE_URL = "http://localhost:8082/api/dishes";

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
        logger.info("Starting order validation for order ID: {}", order.getId());
        OrderValidationMessage message = OrderValidationMessage.fromOrder(order);
        
        rabbitTemplate.convertAndSend(
            OrderValidationConfig.ORDER_VALIDATION_EXCHANGE,
            OrderValidationConfig.STOCK_CHECK_ROUTING_KEY,
            message
        );
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