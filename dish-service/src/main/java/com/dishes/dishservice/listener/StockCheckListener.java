package com.dishes.dishservice.listener;

import com.dishes.dishservice.config.RabbitMQConfig;
import com.dishes.dishservice.dto.OrderValidationMessage;
import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.service.DishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StockCheckListener {
    private static final Logger logger = LoggerFactory.getLogger(StockCheckListener.class);
    
    @Autowired
    private DishService dishService;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @RabbitListener(queues = RabbitMQConfig.STOCK_CHECK_QUEUE)
    public void handleStockCheck(OrderValidationMessage message) {
        logger.info("Dish service received stock check request for order ID: {}", message.getOrderId());
        boolean allItemsInStock = true;
        StringBuilder validationMessageBuilder = new StringBuilder();
        
        for (OrderValidationMessage.OrderItemInfo item : message.getItems()) {
            try {
                Dish dish = dishService.getDish(item.getDishId());
                
                if (dish == null) {
                    logger.error("Dish not found: {}", item.getDishId());
                    allItemsInStock = false;
                    validationMessageBuilder.append("Dish not found: ").append(item.getDishId()).append("; ");
                    continue;
                }
                
                if (!dish.getAvailable()) {
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
        logger.info("Dish service stock check complete: allInStock={}", allItemsInStock);
        
        // Send the result back to the same queue for the order service to pick up
        // The order service will handle the next steps based on this result
        if (allItemsInStock) {
            logger.info("Stock check passed, forwarding to payment validation");
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_VALIDATION_EXCHANGE,
                RabbitMQConfig.PAYMENT_VALIDATION_ROUTING_KEY,
                message
            );
        } else {
            logger.info("Stock check failed, forwarding to order rejection");
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_VALIDATION_EXCHANGE,
                RabbitMQConfig.ORDER_REJECTION_ROUTING_KEY,
                message
            );
        }
    }
    
    @RabbitListener(queues = RabbitMQConfig.ORDER_COMPLETION_QUEUE)
    public void handleOrderCompletion(OrderValidationMessage message) {
        logger.info("Dish service received order completion for order ID: {}", message.getOrderId());
        
        // Update inventory by reducing quantities
        for (OrderValidationMessage.OrderItemInfo item : message.getItems()) {
            try {
                Dish dish = dishService.getDish(item.getDishId());
                
                if (dish != null && dish.getQuantity() != null) {
                    // Update dish quantity
                    int newQuantity = dish.getQuantity() - item.getQuantity();
                    dish.setQuantity(newQuantity);
                    
                    // If new quantity is 0, mark as unavailable
                    if (newQuantity <= 0) {
                        dish.setAvailable(false);
                    }
                    
                    // Update dish
                    dishService.updateDish(dish);
                    logger.info("Updated inventory for dish: {}, new quantity: {}", dish.getName(), newQuantity);
                }
            } catch (Exception e) {
                logger.error("Error updating inventory for dish: {}", item.getDishId(), e);
            }
        }
    }
} 