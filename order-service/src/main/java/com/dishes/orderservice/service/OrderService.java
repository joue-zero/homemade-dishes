package com.dishes.orderservice.service;

import com.dishes.orderservice.model.Order;
import com.dishes.orderservice.model.OrderItem;
import com.dishes.orderservice.repository.OrderRepository;
import com.dishes.orderservice.dto.DishDTO;
import com.dishes.orderservice.dto.UserDTO;
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
    private static final String USER_SERVICE_BASE_URL = "http://localhost:8081/api/users";

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
        Long dishSellerId = null;

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
                    
                    // If this is the first dish, store the seller ID
                    // Assuming all dishes in the order are from the same seller
                    if (dishSellerId == null && dish.getSellerId() != null) {
                        dishSellerId = dish.getSellerId();
                        order.setSellerId(dishSellerId);
                        logger.info("Set seller ID to: {}", dishSellerId);
                    } else if (dish.getSellerId() != null && !dish.getSellerId().equals(dishSellerId)) {
                        // If we found a dish with a different seller ID, log a warning
                        logger.warn("Dish {} has different seller ID {} than previous dishes {}",
                            dishId, dish.getSellerId(), dishSellerId);
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
        logger.info("Saving order with total amount: {}, sellerId: {}, customerName: {}", 
            totalAmount, order.getSellerId(), order.getCustomerName());
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
        logger.info("Creating order directly with provided data: {}", order);
        
        if (order.getCustomerId() == null) {
            logger.error("Customer ID cannot be null");
            throw new RuntimeException("Customer ID cannot be null");
        }
        
        // If customer name is missing, try to fetch it
        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
            logger.info("Customer name not provided, attempting to fetch from user service");
            UserDTO user = fetchUserInfo(order.getCustomerId());
            if (user != null && user.getUsername() != null) {
                order.setCustomerName(user.getUsername());
                logger.info("Set customer name to: {}", user.getUsername());
            }
        }
        
        // Ensure there is a status set
        if (order.getStatus() == null) {
            order.setStatus(Order.OrderStatus.PENDING);
        }
        
        // Make sure all order items have the correct order reference
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
            }
        }
        
        logger.info("Saving order with customer ID: {}, seller ID: {}, customer name: {}", 
            order.getCustomerId(), order.getSellerId(), order.getCustomerName());
        return orderRepository.save(order);
    }

    /**
     * Helper method to fetch user information
     * @param userId ID of the user to fetch
     * @return UserDTO if found, null otherwise
     */
    private UserDTO fetchUserInfo(Long userId) {
        if (userId == null) {
            logger.warn("Cannot fetch user info for null user ID");
            return null;
        }
        
        try {
            // Try direct endpoint first
            String directUrl = USER_SERVICE_BASE_URL + "/" + userId;
            logger.info("Attempting to fetch user info from: {}", directUrl);
            
            try {
                UserDTO user = restTemplate.getForObject(directUrl, UserDTO.class);
                if (user != null && user.getId() != null) {
                    logger.info("Successfully fetched user: {}", user.getUsername());
                    return user;
                }
            } catch (Exception e) {
                logger.warn("Error fetching user from direct endpoint: {}", e.getMessage());
            }
            
            // If direct endpoint fails, try a different format
            String alternateUrl = USER_SERVICE_BASE_URL + "/id/" + userId;
            logger.info("Attempting to fetch user info from alternate endpoint: {}", alternateUrl);
            
            UserDTO user = restTemplate.getForObject(alternateUrl, UserDTO.class);
            if (user != null && user.getId() != null) {
                logger.info("Successfully fetched user from alternate endpoint: {}", user.getUsername());
                return user;
            }
            
            logger.warn("Could not find user with ID: {}", userId);
            return null;
            
        } catch (Exception e) {
            logger.error("Failed to fetch user info for user ID {}: {}", userId, e.getMessage());
            return null;
        }
    }
} 