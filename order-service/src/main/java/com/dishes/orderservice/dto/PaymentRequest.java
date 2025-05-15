package com.dishes.orderservice.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long orderId;
    
    // Getters and setters
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
} 