package com.dishes.userservice.service;

import com.dishes.userservice.config.NotificationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class LoggingService {
    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    private static final String SERVICE_NAME = "User";
    
    @Autowired(required = false)
    @Qualifier("logRabbitTemplate")
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
        
        // Send to RabbitMQ only if available
        if (rabbitTemplate != null) {
            try {
                // Format routing key as "ServiceName_Severity"
                String routingKey = SERVICE_NAME + "_" + severity.name();
                
                // Send to log exchange
                rabbitTemplate.convertAndSend(NotificationConfig.LOG_EXCHANGE, routingKey, message);
            } catch (Exception e) {
                logger.warn("Failed to send log message to RabbitMQ: {}", e.getMessage());
                // Silently continue without failing the application
            }
        }
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