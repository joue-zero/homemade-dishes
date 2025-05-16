package com.dishes.orderservice.service;

import com.dishes.orderservice.model.Order;
import com.dishes.orderservice.model.OrderItem;
import com.dishes.orderservice.repository.OrderRepository;
import com.dishes.orderservice.dto.DishDTO;
import com.dishes.orderservice.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final String DISH_SERVICE_URL = "http://localhost:8082/api/dishes";
    private static final String USER_SERVICE_BASE_URL = "http://localhost:8081/api/users";

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Lazy
    @Autowired
    private OrderValidationService orderValidationService;

    @Transactional
    public Order createOrder(Long userId, List<Map<String, Object>> items) {
        if (userId == null) {
            logger.error("User ID cannot be null");
            throw new RuntimeException("User ID cannot be null");
        }

        if (items == null || items.isEmpty()) {
            logger.error("Order must contain at least one item");
            throw new RuntimeException("Order must contain at least one item");
        }

        // Get user information for customer name
        UserDTO user = fetchUserInfo(userId);

        Order order = new Order();
        order.setCustomerId(userId);
        
        // Set the customer name if available
        if (user != null) {
            order.setCustomerName(user.getUsername());
        }
        
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
                    orderItem.setSellerId(dish.getSellerId());
                    orderItem.setOrder(order);
                    orderItems.add(orderItem);
                    totalAmount = totalAmount.add(BigDecimal.valueOf(orderItem.getSubtotal()));
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
        Order savedOrder = orderRepository.save(order);
        
        // Trigger order validation after saving
        try {
            orderValidationService.startOrderValidation(savedOrder);
        } catch (Exception e) {
            logger.error("Error initiating order validation: {}", e.getMessage(), e);
            // Don't disrupt existing flow by throwing exceptions, just log it
        }
        
        return savedOrder;
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
            
            order.setStatus(status);
            Order savedOrder = orderRepository.save(order);
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
        // Modified to search for orders containing items from a specific seller
        List<Order> allOrders = orderRepository.findAll();
        List<Order> sellerOrders = new ArrayList<>();
        
        for (Order order : allOrders) {
            for (OrderItem item : order.getItems()) {
                if (item.getSellerId() != null && item.getSellerId().equals(sellerId)) {
                    sellerOrders.add(order);
                    break;
                }
            }
        }
        
        return sellerOrders;
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Order createOrder(Order order) {
        if (order.getCustomerId() == null) {
            logger.error("Customer ID cannot be null");
            throw new RuntimeException("Customer ID cannot be null");
        }

        if (order.getCustomerName() == null) {
            UserDTO user = fetchUserInfo(order.getCustomerId());
            if (user != null) {
                order.setCustomerName(user.getUsername());
            }
        }
        
        if (order.getStatus() == null) {
            order.setStatus(Order.OrderStatus.PENDING);
        }
        
        // Save order with items
        Order savedOrder = orderRepository.save(order);
        
        // Start validation if the order is pending
        if (savedOrder.getStatus() == Order.OrderStatus.PENDING) {
            try {
                orderValidationService.startOrderValidation(savedOrder);
            } catch (Exception e) {
                logger.error("Error initiating order validation: {}", e.getMessage(), e);
            }
        }
        
        return savedOrder;
    }

    private UserDTO fetchUserInfo(Long userId) {
        try {
            String userServiceUrl = USER_SERVICE_BASE_URL + "/" + userId;
            return restTemplate.getForObject(userServiceUrl, UserDTO.class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
            // Silently handle 404 errors - user service not available
            return null;
        } catch (Exception e) {
            logger.error("Error fetching user information: {}", e.getMessage(), e);
            return null;
        }
    }
} 