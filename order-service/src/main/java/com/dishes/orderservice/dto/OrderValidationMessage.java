package com.dishes.orderservice.dto;

import com.dishes.orderservice.model.Order;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderValidationMessage implements Serializable {
    private Long orderId;
    private Long customerId;
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
        private Long sellerId;
    }
    
    // Static factory method to create from Order entity
    public static OrderValidationMessage fromOrder(Order order) {
        OrderValidationMessage message = new OrderValidationMessage();
        message.setOrderId(order.getId());
        message.setCustomerId(order.getCustomerId());
        message.setTotalAmount(order.getTotalAmount());
        
        // Convert OrderItems to OrderItemInfo
        List<OrderItemInfo> itemInfos = order.getItems().stream()
                .map(orderItem -> {
                    OrderItemInfo itemInfo = new OrderItemInfo();
                    itemInfo.setDishId(orderItem.getDishId());
                    itemInfo.setDishName(orderItem.getDishName());
                    itemInfo.setQuantity(orderItem.getQuantity());
                    itemInfo.setSellerId(orderItem.getSellerId());
                    return itemInfo;
                })
                .toList();
        
        message.setItems(itemInfos);
        
        return message;
    }
} 