package com.dishes.orderservice.service;

import com.dishes.orderservice.config.NotificationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoggingService {
    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    private static final String SERVICE_NAME = "Order";
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public enum LogSeverity {
        INFO, WARNING, ERROR
    }
    
    public void log(LogSeverity severity, String message) {
        // Log to console
        switch (severity) {
            case INFO:
                logger.info(message);
                break;
            case WARNING:
                logger.warn(message);
                break;
            case ERROR:
                logger.error(message);
                break;
        }
        
        // Format routing key as "ServiceName_Severity"
        String routingKey = SERVICE_NAME + "_" + severity.name();
        
        // Send to log exchange
        rabbitTemplate.convertAndSend(NotificationConfig.LOG_EXCHANGE, routingKey, message);
    }
    
    public void logInfo(String message) {
        log(LogSeverity.INFO, message);
    }
    
    public void logWarning(String message) {
        log(LogSeverity.WARNING, message);
    }
    
    public void logError(String message) {
        log(LogSeverity.ERROR, message);
    }
} 