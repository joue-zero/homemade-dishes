package com.dishes.orderservice.listener;

import com.dishes.orderservice.config.OrderValidationConfig;
import com.dishes.orderservice.dto.OrderValidationMessage;
import com.dishes.orderservice.service.OrderValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderValidationListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderValidationListener.class);
    private static final String SEPARATOR = "=================================================";
    
    @Autowired
    private OrderValidationService orderValidationService;
    
    @RabbitListener(queues = OrderValidationConfig.STOCK_CHECK_QUEUE)
    public void handleStockCheck(OrderValidationMessage message) {
        logRabbitMessage("STOCK CHECK", message.getOrderId());
        
        try {
            // Add 5 second delay to allow time to see messages in RabbitMQ Management UI
            // Thread.sleep(5000);
            
            orderValidationService.checkStock(message);
        } catch (Exception e) {
            logger.error("ERROR in RabbitMQ processing: {}", e.getMessage(), e);
        }
        
        logger.info(SEPARATOR);
    }
    
    @RabbitListener(queues = OrderValidationConfig.PAYMENT_VALIDATION_QUEUE)
    public void handlePaymentValidation(OrderValidationMessage message) {
        logRabbitMessage("PAYMENT VALIDATION", message.getOrderId());
        
        try {
            // Add 5 second delay to allow time to see messages in RabbitMQ Management UI
            // Thread.sleep(5000);
            
            orderValidationService.validatePayment(message);
        } catch (Exception e) {
            logger.error("ERROR in RabbitMQ processing: {}", e.getMessage(), e);
        }
        
        logger.info(SEPARATOR);
    }
    
    @RabbitListener(queues = OrderValidationConfig.ORDER_COMPLETION_QUEUE)
    public void handleOrderCompletion(OrderValidationMessage message) {
        logRabbitMessage("ORDER COMPLETION", message.getOrderId());
        
        try {
            // Add 5 second delay to allow time to see messages in RabbitMQ Management UI
            // Thread.sleep(5000);
            
            orderValidationService.completeOrder(message);
        } catch (Exception e) {
            logger.error("ERROR in RabbitMQ processing: {}", e.getMessage(), e);
        }
        
        logger.info(SEPARATOR);
    }
    
    @RabbitListener(queues = OrderValidationConfig.ORDER_REJECTION_QUEUE)
    public void handleOrderRejection(OrderValidationMessage message) {
        logRabbitMessage("ORDER REJECTION", message.getOrderId());
        
        try {
            // Add 5 second delay to allow time to see messages in RabbitMQ Management UI
            // Thread.sleep(5000);
            
            orderValidationService.rejectOrder(message);
        } catch (Exception e) {
            logger.error("ERROR in RabbitMQ processing: {}", e.getMessage(), e);
        }
        
        logger.info(SEPARATOR);
    }
    
    /**
     * Helper method to log RabbitMQ messages in a formatted, multi-line style
     */
    private void logRabbitMessage(String operation, Long orderId) {
        logger.info(SEPARATOR);
        logger.info("RABBITMQ: {} ", operation);
        logger.info("┌─────────────────────────────────────┐");
        logger.info("│ Operation: {}     │", operation);
        logger.info("│ Order ID: {}                      │", orderId);
        logger.info("│ Status: PROCESSING                  │");
        logger.info("└─────────────────────────────────────┘");
        logger.info(SEPARATOR);
    }
} 