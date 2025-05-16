package com.dishes.orderservice.dto;

import com.dishes.orderservice.model.Order;
import com.dishes.orderservice.model.OrderItem;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class SellerOrderDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private Order.OrderStatus status;
    private Order.PaymentStatus paymentStatus;
    private List<OrderItemDTO> sellerItems = new ArrayList<>();
    private BigDecimal sellerSubtotal;
    private BigDecimal totalOrderAmount;
    private boolean isMultiSellerOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Data
    public static class OrderItemDTO {
        private Long id;
        private Long dishId;
        private String dishName;
        private Double price;
        private Integer quantity;
        private Double subtotal;
    }
    
    public static SellerOrderDTO fromOrder(Order order, Long sellerId) {
        SellerOrderDTO dto = new SellerOrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomerId());
        dto.setCustomerName(order.getCustomerName());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTotalOrderAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        // Count unique sellers in the order
        long uniqueSellers = order.getItems().stream()
                .map(OrderItem::getSellerId)
                .distinct()
                .count();
        
        dto.setMultiSellerOrder(uniqueSellers > 1);
        
        // Filter and convert items for this seller
        BigDecimal sellerSubtotal = BigDecimal.ZERO;
        List<OrderItemDTO> sellerItems = new ArrayList<>();
        
        for (OrderItem item : order.getItems()) {
            if (sellerId.equals(item.getSellerId())) {
                OrderItemDTO itemDto = new OrderItemDTO();
                itemDto.setId(item.getId());
                itemDto.setDishId(item.getDishId());
                itemDto.setDishName(item.getDishName());
                itemDto.setPrice(item.getPrice());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setSubtotal(item.getSubtotal());
                
                sellerItems.add(itemDto);
                sellerSubtotal = sellerSubtotal.add(BigDecimal.valueOf(item.getSubtotal()));
            }
        }
        
        dto.setSellerItems(sellerItems);
        dto.setSellerSubtotal(sellerSubtotal);
        
        return dto;
    }
} 