package com.dishes.orderservice.repository;

import com.dishes.orderservice.model.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserBalanceRepository extends JpaRepository<UserBalance, Long> {
    
    /**
     * Find a user's balance by their user ID
     */
    Optional<UserBalance> findByUserId(Long userId);
} 