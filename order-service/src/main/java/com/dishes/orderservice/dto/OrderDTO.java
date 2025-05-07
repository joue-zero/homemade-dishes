package com.dishes.orderservice.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String status;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> items;
    private UserDTO user;

    @Data
    public static class OrderItemDTO {
        private Long id;
        private Long dishId;
        private String dishName;
        private Double price;
        private Integer quantity;
    }

    @Data
    public static class UserDTO {
        private Long id;
        private String username;
        private String email;
    }
} 