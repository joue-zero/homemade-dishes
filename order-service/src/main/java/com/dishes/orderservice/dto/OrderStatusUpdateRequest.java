package com.dishes.orderservice.dto;

import com.dishes.orderservice.model.Order;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderStatusUpdateRequest {
    private Order.OrderStatus status;

    @JsonCreator
    public OrderStatusUpdateRequest(@JsonProperty("status") String status) {
        try {
            this.status = Order.OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }

    public OrderStatusUpdateRequest() {
        // Default constructor for Jackson
    }
    
    public OrderStatusUpdateRequest(Order.OrderStatus status) {
        this.status = status;
    }

    public Order.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(Order.OrderStatus status) {
        this.status = status;
    }
    
    // Also allow setting as string for JSON deserialization
    public void setStatus(String status) {
        try {
            this.status = Order.OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }
} 