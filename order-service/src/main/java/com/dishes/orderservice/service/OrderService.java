package com.dishes.orderservice.service;

import com.dishes.orderservice.model.Order;
import com.dishes.orderservice.model.OrderItem;
import com.dishes.orderservice.repository.OrderRepository;
import com.dishes.orderservice.dto.DishDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final String DISH_SERVICE_URL = "http://localhost:8082/api/dishes";

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Transactional
    public Order createOrder(Long userId, List<Map<String, Object>> items) {
        logger.info("Creating order for user: {}", userId);
        logger.info("Order items: {}", items);

        if (userId == null) {
            logger.error("User ID cannot be null");
            throw new RuntimeException("User ID cannot be null");
        }

        if (items == null || items.isEmpty()) {
            logger.error("Order must contain at least one item");
            throw new RuntimeException("Order must contain at least one item");
        }

        Order order = new Order();
        order.setCustomerId(userId);
        order.setStatus(Order.OrderStatus.PENDING);
        
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map<String, Object> item : items) {
            try {
                if (item.get("dishId") == null) {
                    logger.error("Missing dishId in item: {}", item);
                    throw new RuntimeException("Missing dishId in order item");
                }
                
                if (item.get("quantity") == null) {
                    logger.error("Missing quantity in item: {}", item);
                    throw new RuntimeException("Missing quantity in order item");
                }
                
                Long dishId = Long.valueOf(item.get("dishId").toString());
                Integer quantity = Integer.valueOf(item.get("quantity").toString());
                logger.info("Processing dish: id={}, quantity={}", dishId, quantity);

                // Get dish details from dish service
                String dishServiceUrl = DISH_SERVICE_URL + "/" + dishId;
                try {
                    DishDTO dish = restTemplate.getForObject(dishServiceUrl, DishDTO.class);

                    if (dish == null) {
                        logger.error("Dish not found with id: {}", dishId);
                        throw new RuntimeException("Dish not found with id: " + dishId);
                    }

                    if (!dish.isAvailable()) {
                        logger.error("Dish is not available: {}", dish.getName());
                        throw new RuntimeException("Dish is not available: " + dish.getName());
                    }

                    OrderItem orderItem = new OrderItem();
                    orderItem.setDishId(dishId);
                    orderItem.setDishName(dish.getName());
                    orderItem.setPrice(dish.getPrice().doubleValue());
                    orderItem.setQuantity(quantity);
                    orderItem.setSubtotal(dish.getPrice().doubleValue() * quantity);
                    orderItem.setOrder(order);
                    orderItems.add(orderItem);
                    totalAmount = totalAmount.add(BigDecimal.valueOf(orderItem.getSubtotal()));
                    logger.info("Added item to order: {}", orderItem);
                } catch (Exception e) {
                    logger.error("Error fetching dish from dish service: {}", e.getMessage(), e);
                    throw new RuntimeException("Error fetching dish details: " + e.getMessage());
                }
            } catch (Exception e) {
                logger.error("Error processing order item: {}", item, e);
                throw new RuntimeException("Error processing order item: " + e.getMessage());
            }
        }

        if (orderItems.isEmpty()) {
            throw new RuntimeException("No valid items in the order");
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        logger.info("Saving order with total amount: {}", totalAmount);
        return orderRepository.save(order);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByCustomerId(userId);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        logger.info("Service: Updating order status for orderId={} to {}", orderId, status);
        
        if (orderId == null) {
            logger.error("Order ID cannot be null");
            throw new RuntimeException("Order ID cannot be null");
        }
        
        if (status == null) {
            logger.error("Order status cannot be null");
            throw new RuntimeException("Order status cannot be null");
        }
        
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> {
                        logger.error("Order not found with id: {}", orderId);
                        return new RuntimeException("Order not found with id: " + orderId);
                    });
            
            // Log the current and new status
            logger.info("Changing order status from {} to {}", order.getStatus(), status);
            order.setStatus(status);
            
            Order savedOrder = orderRepository.save(order);
            logger.info("Order status updated successfully: {}", savedOrder);
            return savedOrder;
        } catch (Exception e) {
            logger.error("Error updating order status: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating order status: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() == Order.OrderStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed order");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public List<Order> getOrdersBySeller(Long sellerId) {
        return orderRepository.findBySellerId(sellerId);
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }
} 