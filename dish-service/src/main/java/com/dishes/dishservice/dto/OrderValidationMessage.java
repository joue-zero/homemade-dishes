package com.dishes.dishservice.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderValidationMessage implements Serializable {
    private Long orderId;
    private Long customerId;
    private Long sellerId;
    private BigDecimal totalAmount;
    private List<OrderItemInfo> items;
    private boolean stockAvailable = false;
    private boolean paymentValidated = false;
    private String validationMessage;
    
    @Data
    public static class OrderItemInfo implements Serializable {
        private Long dishId;
        private String dishName;
        private Integer quantity;
    }
} 