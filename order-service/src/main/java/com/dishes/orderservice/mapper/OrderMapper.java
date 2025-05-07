package com.dishes.orderservice.mapper;

import com.dishes.orderservice.dto.OrderDTO;
import com.dishes.orderservice.model.Order;
import com.dishes.orderservice.model.OrderItem;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    
    public OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus().toString());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Map order items
        dto.setItems(order.getItems().stream()
                .map(this::toOrderItemDTO)
                .collect(Collectors.toList()));

        // Map user (only ID for now, as we don't have user details in the order)
        if (order.getUserId() != null) {
            OrderDTO.UserDTO userDTO = new OrderDTO.UserDTO();
            userDTO.setId(order.getUserId());
            dto.setUser(userDTO);
        }

        return dto;
    }

    private OrderDTO.OrderItemDTO toOrderItemDTO(OrderItem item) {
        OrderDTO.OrderItemDTO dto = new OrderDTO.OrderItemDTO();
        dto.setId(item.getId());
        dto.setDishId(item.getDishId());
        dto.setDishName(item.getDishName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        return dto;
    }
} 