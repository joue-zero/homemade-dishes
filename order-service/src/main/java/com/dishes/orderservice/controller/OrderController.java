package com.dishes.orderservice.controller;

import com.dishes.orderservice.dto.OrderDTO;
import com.dishes.orderservice.mapper.OrderMapper;
import com.dishes.orderservice.model.Order;
import com.dishes.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody List<Map<String, Object>> items) {
        logger.info("Received order creation request for user: {}", userId);
        logger.info("Order items: {}", items);
        try {
            Order order = orderService.createOrder(userId, items);
            logger.info("Order created successfully: {}", order);
            return ResponseEntity.ok(orderMapper.toDTO(order));
        } catch (Exception e) {
            logger.error("Error creating order", e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getUserOrders(@RequestHeader("X-User-Id") Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        return ResponseEntity.ok(orderMapper.toDTO(order));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        Order order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(orderMapper.toDTO(order));
    }

    @PostMapping("/{id}/cancel")
    // public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id) {
    //     // Order order = orderService.cancelOrder(id);
    //     // return ResponseEntity.ok(orderMapper.toDTO(order));
    //     return ResponseEntity.ok("Order cancelled successfully");
    // }
    
    public String cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return "Order cancelled successfully";
    }
} 