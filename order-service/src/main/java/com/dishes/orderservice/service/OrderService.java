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

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setOrderItems(new ArrayList<>());

        double totalAmount = 0.0;

        for (Map<String, Object> item : items) {
            try {
                Long dishId = Long.valueOf(item.get("dishId").toString());
                int quantity = Integer.parseInt(item.get("quantity").toString());
                logger.info("Processing dish: id={}, quantity={}", dishId, quantity);

                // Fetch dish details from dish service
                DishDTO dish = restTemplate.getForObject(
                    DISH_SERVICE_URL + "/" + dishId,
                    DishDTO.class
                );

                if (dish == null) {
                    logger.error("Dish not found with id: {}", dishId);
                    throw new RuntimeException("Dish not found with id: " + dishId);
                }

                if (!dish.isAvailable()) {
                    logger.error("Dish is not available: {}", dish.getName());
                    throw new RuntimeException("Dish is not available: " + dish.getName());
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setDishId(dishId);
                orderItem.setDishName(dish.getName());
                orderItem.setPrice(dish.getPrice().doubleValue());
                orderItem.setQuantity(quantity);
                orderItem.setSubtotal(dish.getPrice().doubleValue() * quantity);

                order.getOrderItems().add(orderItem);
                totalAmount += orderItem.getSubtotal();
                logger.info("Added item to order: {}", orderItem);
            } catch (Exception e) {
                logger.error("Error processing order item: {}", item, e);
                throw new RuntimeException("Error processing order item: " + e.getMessage());
            }
        }

        order.setTotalAmount(totalAmount);
        logger.info("Saving order with total amount: {}", totalAmount);
        return orderRepository.save(order);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = getOrder(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
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
} 