package com.dishes.orderservice.repository;

import com.dishes.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByCustomerIdAndStatus(Long customerId, Order.OrderStatus status);
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
} 