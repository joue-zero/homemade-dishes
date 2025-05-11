package com.dishes.orderservice.controller;

import com.dishes.orderservice.dto.OrderDTO;
import com.dishes.orderservice.dto.OrderStatusUpdateRequest;
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
        logger.info("Fetching orders for user ID: {}", userId);
        List<Order> orders = orderService.getUserOrders(userId);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(orderMapper::toDTO)
                .collect(Collectors.toList());
        logger.info("Found {} orders for user ID: {}", orders.size(), userId);
        return ResponseEntity.ok(orderDTOs);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        logger.info("Fetching order with ID: {}", orderId);
        Order order = orderService.getOrder(orderId);
        logger.info("Found order: {}", order);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateRequest request) {
        logger.info("Updating status of order {} to {}", orderId, request.getStatus());
        try {
            if (orderId == null) {
                logger.error("Order ID cannot be null");
                return ResponseEntity.badRequest().body("Order ID cannot be null");
            }
            
            if (request == null || request.getStatus() == null) {
                logger.error("Order status cannot be null");
                return ResponseEntity.badRequest().body("Order status cannot be null");
            }
            
            Order updatedOrder = orderService.updateOrderStatus(orderId, request.getStatus());
            logger.info("Order status updated successfully: {}", updatedOrder);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            logger.error("Error updating order status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating order status: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id) {
        logger.info("Cancelling order with ID: {}", id);
        orderService.cancelOrder(id);
        logger.info("Order cancelled successfully");
        return "Order cancelled successfully";
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Order>> getOrdersBySeller(@PathVariable Long sellerId) {
        logger.info("Fetching orders for seller ID: {}", sellerId);
        List<Order> orders = orderService.getOrdersBySeller(sellerId);
        logger.info("Found {} orders for seller ID: {}", orders.size(), sellerId);
        
        // Log order details for debugging
        if (orders.isEmpty()) {
            logger.warn("No orders found for seller ID: {}", sellerId);
        } else {
            for (Order order : orders) {
                logger.debug("Order: id={}, status={}, customerId={}, items={}", 
                    order.getId(), order.getStatus(), order.getCustomerId(), order.getItems().size());
            }
        }
        
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable Long customerId) {
        logger.info("Fetching orders for customer ID: {}", customerId);
        List<Order> orders = orderService.getOrdersByCustomer(customerId);
        logger.info("Found {} orders for customer ID: {}", orders.size(), customerId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        logger.info("Creating order directly: {}", order);
        Order createdOrder = orderService.createOrder(order);
        logger.info("Order created successfully: {}", createdOrder);
        return ResponseEntity.ok(createdOrder);
    }
} 