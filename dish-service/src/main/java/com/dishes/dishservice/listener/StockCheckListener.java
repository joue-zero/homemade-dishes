package com.dishes.dishservice.listener;

import com.dishes.dishservice.config.RabbitMQConfig;
import com.dishes.dishservice.dto.OrderValidationMessage;
import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.service.ejb.DishServiceLocal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "spring.rabbitmq.listener.simple.auto-startup", havingValue = "true")
public class StockCheckListener {
    private static final Logger logger = LoggerFactory.getLogger(StockCheckListener.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private DishServiceLocal dishService;

    @RabbitListener(queues = RabbitMQConfig.STOCK_CHECK_QUEUE)
    public void handleStockCheckMessage(String message) {
        try {
            OrderValidationMessage validationMessage = objectMapper.readValue(message, OrderValidationMessage.class);
            logger.info("Processing stock check for order: {}", validationMessage.getOrderId());
            
            boolean allItemsAvailable = true;
            StringBuilder issueDetails = new StringBuilder();
            
            // Check each order item
            for (OrderValidationMessage.OrderItem item : validationMessage.getItems()) {
                try {
                    Dish dish = dishService.getDish(item.getDishId());
                    
                    if (!dish.getAvailable()) {
                        allItemsAvailable = false;
                        issueDetails.append("Dish ").append(dish.getName())
                                .append(" (ID: ").append(dish.getId()).append(") is not available. ");
                        continue;
                    }
                    
                    if (dish.getQuantity() < item.getQuantity()) {
                        allItemsAvailable = false;
                        issueDetails.append("Dish ").append(dish.getName())
                                .append(" (ID: ").append(dish.getId()).append(") has only ")
                                .append(dish.getQuantity()).append(" items available, but ")
                                .append(item.getQuantity()).append(" were requested. ");
                    }
                } catch (Exception e) {
                    allItemsAvailable = false;
                    issueDetails.append("Error checking dish ").append(item.getDishId())
                            .append(": ").append(e.getMessage()).append(". ");
                }
            }
            
            // Prepare response
            validationMessage.setValid(allItemsAvailable);
            validationMessage.setIssue(issueDetails.toString());
            
            // Send response back
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_VALIDATION_EXCHANGE, 
                allItemsAvailable ? RabbitMQConfig.PAYMENT_VALIDATION_ROUTING_KEY : RabbitMQConfig.ORDER_REJECTION_ROUTING_KEY, 
                objectMapper.writeValueAsString(validationMessage));
            
            logger.info("Stock check completed for order {}: {}", validationMessage.getOrderId(), 
                        allItemsAvailable ? "VALID" : "INVALID");
            
        } catch (Exception e) {
            logger.error("Error processing stock check message", e);
        }
    }
    
    @RabbitListener(queues = RabbitMQConfig.ORDER_COMPLETION_QUEUE)
    public void handleOrderConfirmedMessage(String message) {
        try {
            OrderValidationMessage validationMessage = objectMapper.readValue(message, OrderValidationMessage.class);
            logger.info("Processing stock update for confirmed order: {}", validationMessage.getOrderId());
            
            // Update stock for each ordered item
            for (OrderValidationMessage.OrderItem item : validationMessage.getItems()) {
                try {
                    // Get the dish
                    Dish dish = dishService.getDish(item.getDishId());
                    
                    // Calculate new quantity
                    int newQuantity = dish.getQuantity() - item.getQuantity();
                    if (newQuantity < 0) {
                        newQuantity = 0;
                    }
                    
                    // Update dish quantity
                    dish.setQuantity(newQuantity);
                    
                    // If quantity is zero, mark as unavailable
                    if (newQuantity == 0) {
                        dish.setAvailable(false);
                    }
                    
                    // Save the updated dish
                    dishService.updateDish(dish);
                    
                    logger.info("Updated stock for dish {}: new quantity = {}", dish.getId(), newQuantity);
                } catch (Exception e) {
                    logger.error("Error updating stock for dish {}: {}", item.getDishId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error processing order confirmed message", e);
        }
    }
} 