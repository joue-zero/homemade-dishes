package com.dishes.orderservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long orderId;
    private boolean success;
    private String message;
    private String transactionId;
    private LocalDateTime timestamp;
    
    // Static factory methods for common responses
    public static PaymentResponse success(Long orderId) {
        PaymentResponse response = new PaymentResponse();
        response.setOrderId(orderId);
        response.setSuccess(true);
        response.setMessage("Payment processed successfully");
        response.setTransactionId("TX-" + System.currentTimeMillis());
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
    
    public static PaymentResponse failed(Long orderId, String reason) {
        PaymentResponse response = new PaymentResponse();
        response.setOrderId(orderId);
        response.setSuccess(false);
        response.setMessage(reason);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
    
    // Getters and setters
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
} 