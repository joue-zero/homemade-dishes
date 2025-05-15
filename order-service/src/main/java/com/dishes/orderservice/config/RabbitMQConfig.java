package com.dishes.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class RabbitMQConfig {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);
    
    @Value("${spring.rabbitmq.host}")
    private String host;
    
    @Value("${spring.rabbitmq.port}")
    private int port;
    
    @Value("${spring.rabbitmq.username}")
    private String username;
    
    @Value("${spring.rabbitmq.password}")
    private String password;
    
    public static final String ORDER_QUEUE = "order.queue";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_ROUTING_KEY = "order.routing.key";

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE);
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue orderQueue, DirectExchange orderExchange) {
        return BindingBuilder
                .bind(orderQueue)
                .to(orderExchange)
                .with(ORDER_ROUTING_KEY);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public ConnectionFactory connectionFactory() {
        try {
            CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
            connectionFactory.setHost(host);
            connectionFactory.setPort(port);
            connectionFactory.setUsername(username);
            connectionFactory.setPassword(password);
            // Set a short timeout to fail fast if RabbitMQ is not available
            connectionFactory.setConnectionTimeout(3000);
            return connectionFactory;
        } catch (Exception e) {
            logger.warn("Failed to create RabbitMQ connection factory: {}", e.getMessage());
            return null;
        }
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate() {
        try {
            ConnectionFactory factory = connectionFactory();
            if (factory != null) {
                RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
                rabbitTemplate.setMessageConverter(jsonMessageConverter());
                return rabbitTemplate;
            }
            logger.warn("RabbitMQ connection factory is null, returning null RabbitTemplate");
            return null;
        } catch (Exception e) {
            logger.warn("Failed to create RabbitTemplate: {}", e.getMessage());
            return null;
        }
    }
} 