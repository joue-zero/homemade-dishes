package com.dishes.orderservice.service;

import com.dishes.orderservice.dto.BalanceDTO;
import com.dishes.orderservice.model.UserBalance;
import com.dishes.orderservice.repository.UserBalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class BalanceService {
    private static final Logger logger = LoggerFactory.getLogger(BalanceService.class);
    
    @Value("${user.default.balance:100.0}")
    private BigDecimal defaultBalance;
    
    @Autowired
    private UserBalanceRepository userBalanceRepository;
    
    /**
     * Get a user's balance, initializing if it doesn't exist
     */
    @Transactional
    public BalanceDTO getUserBalance(Long userId) {
        logger.info("Getting balance for user ID: {}", userId);
        
        UserBalance balance = userBalanceRepository.findByUserId(userId)
                .orElseGet(() -> initializeBalance(userId));
        
        BalanceDTO balanceDTO = new BalanceDTO();
        balanceDTO.setUserId(userId);
        balanceDTO.setBalance(balance.getBalance());
        
        return balanceDTO;
    }
    
    /**
     * Initialize a new user balance with the default amount
     */
    @Transactional
    public UserBalance initializeBalance(Long userId) {
        logger.info("Initializing balance for user ID: {}", userId);
        
        UserBalance newBalance = new UserBalance();
        newBalance.setUserId(userId);
        newBalance.setBalance(defaultBalance);
        
        return userBalanceRepository.save(newBalance);
    }
    
    /**
     * Update a user's balance
     */
    @Transactional
    public BalanceDTO updateBalance(Long userId, BigDecimal amount) {
        logger.info("Updating balance for user ID: {}", userId);
        
        UserBalance balance = userBalanceRepository.findByUserId(userId)
                .orElseGet(() -> initializeBalance(userId));
        
        BigDecimal newBalance = balance.getBalance().subtract(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        
        balance.setBalance(newBalance);
        userBalanceRepository.save(balance);
        
        BalanceDTO balanceDTO = new BalanceDTO();
        balanceDTO.setUserId(userId);
        balanceDTO.setBalance(newBalance);
        
        return balanceDTO;
    }
    
    /**
     * Check if a user has sufficient balance
     */
    public boolean hasSufficientBalance(Long userId, BigDecimal amount) {
        UserBalance balance = userBalanceRepository.findByUserId(userId)
                .orElseGet(() -> initializeBalance(userId));
        
        return balance.getBalance().compareTo(amount) >= 0;
    }
} 