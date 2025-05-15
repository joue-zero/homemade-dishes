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
    
    @Autowired
    private OrderValidationService orderValidationService;
    
    @RabbitListener(queues = OrderValidationConfig.STOCK_CHECK_QUEUE)
    public void handleStockCheck(OrderValidationMessage message) {
        logger.info("Received stock check request for order ID: {}", message.getOrderId());
        try {
            orderValidationService.checkStock(message);
            logger.info("Stock check completed for order ID: {}", message.getOrderId());
        } catch (Exception e) {
            logger.error("Error processing stock check for order ID: {}: {}", message.getOrderId(), e.getMessage(), e);
        }
    }
    
    @RabbitListener(queues = OrderValidationConfig.PAYMENT_VALIDATION_QUEUE)
    public void handlePaymentValidation(OrderValidationMessage message) {
        logger.info("Received payment validation request for order ID: {}", message.getOrderId());
        try {
            orderValidationService.validatePayment(message);
            logger.info("Payment validation completed for order ID: {}", message.getOrderId());
        } catch (Exception e) {
            logger.error("Error processing payment validation for order ID: {}: {}", message.getOrderId(), e.getMessage(), e);
        }
    }
    
    @RabbitListener(queues = OrderValidationConfig.ORDER_COMPLETION_QUEUE)
    public void handleOrderCompletion(OrderValidationMessage message) {
        logger.info("Received order completion request for order ID: {}", message.getOrderId());
        try {
            orderValidationService.completeOrder(message);
            logger.info("Order completion processed for order ID: {}", message.getOrderId());
        } catch (Exception e) {
            logger.error("Error processing order completion for order ID: {}: {}", message.getOrderId(), e.getMessage(), e);
        }
    }
    
    @RabbitListener(queues = OrderValidationConfig.ORDER_REJECTION_QUEUE)
    public void handleOrderRejection(OrderValidationMessage message) {
        logger.info("Received order rejection request for order ID: {}", message.getOrderId());
        try {
            orderValidationService.rejectOrder(message);
            logger.info("Order rejection processed for order ID: {}", message.getOrderId());
        } catch (Exception e) {
            logger.error("Error processing order rejection for order ID: {}: {}", message.getOrderId(), e.getMessage(), e);
        }
    }
} 