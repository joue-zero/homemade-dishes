package com.dishes.orderservice.service;

import com.dishes.orderservice.dto.BalanceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class BalanceService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceService.class);
    private static final String USER_SERVICE_URL = "http://localhost:8081/api/users";
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Get a user's balance from the user service
     */
    @Transactional
    public BalanceDTO getUserBalance(Long userId) {
        logger.info("Getting balance for user ID: {} from user service", userId);
        
        try {
            String balanceUrl = USER_SERVICE_URL + "/" + userId + "/balance";
            BigDecimal balance = restTemplate.getForObject(balanceUrl, BigDecimal.class);
            
            BalanceDTO balanceDTO = new BalanceDTO();
            balanceDTO.setUserId(userId);
            balanceDTO.setBalance(balance);
            
            return balanceDTO;
        } catch (Exception e) {
            logger.error("Error fetching balance from user service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get user balance: " + e.getMessage());
        }
    }
    
    /**
     * Update a user's balance by calling user service
     */
    @Transactional
    public BalanceDTO updateBalance(Long userId, BigDecimal amount) {
        logger.info("Updating balance for user ID: {} with amount: {} via user service", userId, amount);
        
        try {
            String balanceUrl = USER_SERVICE_URL + "/" + userId + "/balance";
            
            // Create payload for balance update
            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", amount);
            
            // Call user service to update balance
            BigDecimal newBalance = restTemplate.postForObject(balanceUrl, payload, BigDecimal.class);
            
            BalanceDTO balanceDTO = new BalanceDTO();
            balanceDTO.setUserId(userId);
            balanceDTO.setBalance(newBalance);
            
            return balanceDTO;
        } catch (Exception e) {
            logger.error("Error updating balance via user service: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Failed to update balance: " + e.getMessage());
        }
    }
    
    /**
     * Check if a user has sufficient balance by calling user service
     */
    public boolean hasSufficientBalance(Long userId, BigDecimal amount) {
        try {
            String balanceUrl = USER_SERVICE_URL + "/" + userId + "/balance";
            BigDecimal balance = restTemplate.getForObject(balanceUrl, BigDecimal.class);
            
            return balance != null && balance.compareTo(amount) >= 0;
        } catch (Exception e) {
            logger.error("Error checking balance via user service: {}", e.getMessage(), e);
            return false;
        }
    }
} 