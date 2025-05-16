package com.dishes.dishservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DishServiceApplication {
    public static void main(String[] args) {
        // To enable EJB support, set ejb.enabled=true in application.properties
        // EJB endpoints will be available at /api/ejb/dishes
        SpringApplication.run(DishServiceApplication.class, args);
    }
} 