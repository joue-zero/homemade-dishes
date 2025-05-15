package com.dishes.orderservice.controller;

import com.dishes.orderservice.dto.BalanceDTO;
import com.dishes.orderservice.service.BalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/balance")
public class BalanceController {
    private static final Logger logger = LoggerFactory.getLogger(BalanceController.class);
    
    @Autowired
    private BalanceService balanceService;
    
    /**
     * Get a user's balance
     */
    @GetMapping
    public ResponseEntity<BalanceDTO> getUserBalance(@RequestHeader("X-User-Id") Long userId) {
        logger.info("Getting balance for user ID: {}", userId);
        BalanceDTO balance = balanceService.getUserBalance(userId);
        return ResponseEntity.ok(balance);
    }
    
    /**
     * Check if a user has sufficient balance for an amount
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkBalance(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("amount") BigDecimal amount) {
        logger.info("Checking balance for user ID: {} and amount: {}", userId, amount);
        boolean hasSufficientBalance = balanceService.hasSufficientBalance(userId, amount);
        return ResponseEntity.ok(hasSufficientBalance);
    }
    
    /**
     * Update a user's balance (subtract amount)
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateBalance(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam("amount") BigDecimal amount) {
        logger.info("Updating balance for user ID: {} and amount: {}", userId, amount);
        
        try {
            BalanceDTO updatedBalance = balanceService.updateBalance(userId, amount);
            return ResponseEntity.ok(updatedBalance);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating balance: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 