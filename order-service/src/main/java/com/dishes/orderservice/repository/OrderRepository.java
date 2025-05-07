package com.dishes.orderservice.repository;

import com.dishes.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
} 