package com.dishes.userservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {
    private static final Logger logger = LoggerFactory.getLogger(NotificationConfig.class);
    
    // Logging exchange (topic exchange)
    public static final String LOG_EXCHANGE = "log.exchange";
    
    // Define exchanges
    @Bean
    @ConditionalOnProperty(name = "spring.rabbitmq.host")
    public TopicExchange logExchange() {
        try {
            return new TopicExchange(LOG_EXCHANGE);
        } catch (Exception e) {
            logger.warn("Failed to create log exchange: {}", e.getMessage());
            return null;
        }
    }
    
    // Message converter for RabbitMQ
    @Bean
    @ConditionalOnProperty(name = "spring.rabbitmq.host")
    public MessageConverter logMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    @ConditionalOnBean(ConnectionFactory.class)
    public RabbitTemplate logRabbitTemplate(ConnectionFactory connectionFactory) {
        try {
            RabbitTemplate template = new RabbitTemplate(connectionFactory);
            template.setMessageConverter(logMessageConverter());
            return template;
        } catch (Exception e) {
            logger.warn("Failed to create RabbitTemplate: {}", e.getMessage());
            return null;
        }
    }
} 