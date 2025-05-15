package com.dishes.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderValidationConfig {
    
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
    
    // Create exchange
    @Bean
    public DirectExchange orderValidationExchange() {
        return new DirectExchange(ORDER_VALIDATION_EXCHANGE);
    }
    
    // Create queues
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
    
    // Create bindings
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
} 