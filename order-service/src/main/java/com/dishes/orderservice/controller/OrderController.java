package com.dishes.orderservice.controller;

import com.dishes.orderservice.dto.OrderDTO;
import com.dishes.orderservice.mapper.OrderMapper;
import com.dishes.orderservice.model.Order;
import com.dishes.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/user-order")
    public ResponseEntity<?> createUserOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody List<Map<String, Object>> items) {
        logger.info("Received order creation request for user: {}", userId);
        logger.info("Order items: {}", items);
        try {
            if (items == null || items.isEmpty()) {
                return ResponseEntity.badRequest().body("Order must contain at least one item");
            }
            
            // Validate each item has dishId and quantity
            for (Map<String, Object> item : items) {
                if (item.get("dishId") == null) {
                    return ResponseEntity.badRequest().body("Missing dishId in order item");
                }
                if (item.get("quantity") == null) {
                    return ResponseEntity.badRequest().body("Missing quantity in order item");
                }
            }
            
            Order order = orderService.createOrder(userId, items);
            logger.info("Order created successfully: {}", order);
            return ResponseEntity.ok(orderMapper.toDTO(order));
        } catch (Exception e) {
            logger.error("Error creating order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating order: " + e.getMessage());
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

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request.getStatus()));
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return "Order cancelled successfully";
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Order>> getOrdersBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(orderService.getOrdersBySeller(sellerId));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }
}

class OrderStatusUpdateRequest {
    private Order.OrderStatus status;

    public Order.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(Order.OrderStatus status) {
        this.status = status;
    }
} 