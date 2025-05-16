package com.dishes.orderservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {
    private static final Logger logger = LoggerFactory.getLogger(NotificationConfig.class);
    
    // Payment notification exchange (direct exchange)
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";
    public static final String ADMIN_PAYMENT_NOTIFICATION_QUEUE = "admin.payment.notification.queue";
    
    // Logging exchange (topic exchange)
    public static final String LOG_EXCHANGE = "log.exchange";
    public static final String ADMIN_LOG_NOTIFICATION_QUEUE = "admin.log.notification.queue";
    
    // Define exchanges
    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE);
    }
    
    @Bean
    public TopicExchange logExchange() {
        return new TopicExchange(LOG_EXCHANGE);
    }
    
    // Define queues
    @Bean
    public Queue adminPaymentNotificationQueue() {
        return new Queue(ADMIN_PAYMENT_NOTIFICATION_QUEUE);
    }
    
    @Bean
    public Queue adminLogNotificationQueue() {
        return new Queue(ADMIN_LOG_NOTIFICATION_QUEUE);
    }
    
    // Define bindings
    @Bean
    public Binding adminPaymentNotificationBinding() {
        return BindingBuilder
                .bind(adminPaymentNotificationQueue())
                .to(paymentExchange())
                .with(PAYMENT_FAILED_ROUTING_KEY);
    }
    
    @Bean
    public Binding adminLogNotificationBinding() {
        return BindingBuilder
                .bind(adminLogNotificationQueue())
                .to(logExchange())
                .with("*_Error"); // Bind to all error logs from any service
    }
} 