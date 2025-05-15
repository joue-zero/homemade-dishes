package com.dishes.orderservice.config;

import com.dishes.orderservice.dto.OrderValidationMessage;
import com.dishes.orderservice.service.OrderValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

@Configuration
public class OrderValidationConfig {
    private static final Logger logger = LoggerFactory.getLogger(OrderValidationConfig.class);
    
    // Exchange names
    public static final String ORDER_VALIDATION_EXCHANGE = "order.validation.exchange";
    
    // Queue names
    public static final String STOCK_CHECK_QUEUE = "order.stock.check.queue";
    public static final String PAYMENT_VALIDATION_QUEUE = "order.payment.validation.queue";
    public static final String ORDER_COMPLETION_QUEUE = "order.completion.queue";
    public static final String ORDER_REJECTION_QUEUE = "order.rejection.queue";
    
    // Routing keys
    public static final String STOCK_CHECK_ROUTING_KEY = "order.stock.check";
    public static final String PAYMENT_VALIDATION_ROUTING_KEY = "order.payment.validation";
    public static final String ORDER_COMPLETION_ROUTING_KEY = "order.completion";
    public static final String ORDER_REJECTION_ROUTING_KEY = "order.rejection";
    
    @Autowired
    private OrderValidationService orderValidationService;

    // Define exchange
    @Bean
    public DirectExchange orderValidationExchange() {
        return new DirectExchange(ORDER_VALIDATION_EXCHANGE);
    }
    
    // Define queues
    @Bean
    public Queue stockCheckQueue() {
        return new Queue(STOCK_CHECK_QUEUE);
    }
    
    @Bean
    public Queue paymentValidationQueue() {
        return new Queue(PAYMENT_VALIDATION_QUEUE);
    }
    
    @Bean
    public Queue orderCompletionQueue() {
        return new Queue(ORDER_COMPLETION_QUEUE);
    }
    
    @Bean
    public Queue orderRejectionQueue() {
        return new Queue(ORDER_REJECTION_QUEUE);
    }
    
    // Define bindings
    @Bean
    public Binding stockCheckBinding() {
        return BindingBuilder
                .bind(stockCheckQueue())
                .to(orderValidationExchange())
                .with(STOCK_CHECK_ROUTING_KEY);
    }
    
    @Bean
    public Binding paymentValidationBinding() {
        return BindingBuilder
                .bind(paymentValidationQueue())
                .to(orderValidationExchange())
                .with(PAYMENT_VALIDATION_ROUTING_KEY);
    }
    
    @Bean
    public Binding orderCompletionBinding() {
        return BindingBuilder
                .bind(orderCompletionQueue())
                .to(orderValidationExchange())
                .with(ORDER_COMPLETION_ROUTING_KEY);
    }
    
    @Bean
    public Binding orderRejectionBinding() {
        return BindingBuilder
                .bind(orderRejectionQueue())
                .to(orderValidationExchange())
                .with(ORDER_REJECTION_ROUTING_KEY);
    }
    
    // RabbitMQ Listeners
    @RabbitListener(queues = STOCK_CHECK_QUEUE)
    public void receiveStockCheckMessage(OrderValidationMessage message) {
        try {
            logger.info("Received stock check request for order ID: {}", message.getOrderId());
            orderValidationService.checkStock(message);
        } catch (Exception e) {
            logger.error("Error processing stock check message: {}", e.getMessage(), e);
        }
    }
    
    @RabbitListener(queues = PAYMENT_VALIDATION_QUEUE)
    public void receivePaymentValidationMessage(OrderValidationMessage message) {
        try {
            logger.info("Received payment validation request for order ID: {}", message.getOrderId());
            orderValidationService.validatePayment(message);
        } catch (Exception e) {
            logger.error("Error processing payment validation message: {}", e.getMessage(), e);
        }
    }
    
    @RabbitListener(queues = ORDER_COMPLETION_QUEUE)
    public void receiveOrderCompletionMessage(OrderValidationMessage message) {
        try {
            logger.info("Received order completion request for order ID: {}", message.getOrderId());
            orderValidationService.completeOrder(message);
        } catch (Exception e) {
            logger.error("Error processing order completion message: {}", e.getMessage(), e);
        }
    }
    
    @RabbitListener(queues = ORDER_REJECTION_QUEUE)
    public void receiveOrderRejectionMessage(OrderValidationMessage message) {
        try {
            logger.info("Received order rejection request for order ID: {}", message.getOrderId());
            orderValidationService.rejectOrder(message);
        } catch (Exception e) {
            logger.error("Error processing order rejection message: {}", e.getMessage(), e);
        }
    }
} 