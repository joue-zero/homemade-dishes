package com.dishes.dishservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.ejb.embeddable.EJBContainer;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class to initialize and manage EJB container within Spring Boot application
 */
@Configuration
public class EjbConfig {
    private static final Logger logger = LoggerFactory.getLogger(EjbConfig.class);
    
    @Bean(destroyMethod = "close")
    public EJBContainer ejbContainer() {
        // Configure the EJB container
        Map<String, Object> properties = new HashMap<>();
        
        // Specify where to look for EJB modules
        properties.put(EJBContainer.MODULES, "dish-service");

        try {
            logger.info("Starting embedded EJB container");
            return EJBContainer.createEJBContainer(properties);
        } catch (Exception e) {
            logger.error("Error starting EJB container", e);
            throw new RuntimeException("Failed to start EJB container", e);
        }
    }
    
    @Bean
    public Context ejbContext(EJBContainer container) throws NamingException {
        return container.getContext();
    }
} 