package com.dishes.userservice.controller;

import com.dishes.userservice.model.User;
import com.dishes.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired // this is used to inject the UserRepository bean into the UserController class
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        return ResponseEntity.ok(userRepository.save(user));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // check if user credentials are correct giving user name and password
    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody User user) {
        return userRepository.findByUsername(user.getUsername())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // DELETE endpoint that accepts POST method for deleting users
    @PostMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Get user balance
    @GetMapping("/{userId}/balance")
    public ResponseEntity<BigDecimal> getUserBalance(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(user.getBalance()))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Update user balance
    @PostMapping("/{userId}/balance")
    public ResponseEntity<BigDecimal> updateUserBalance(@PathVariable Long userId, @RequestBody Map<String, Object> payload) {
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        
        return userRepository.findById(userId)
                .map(user -> {
                    BigDecimal currentBalance = user.getBalance();
                    BigDecimal newBalance = currentBalance.subtract(amount);
                    
                    // Ensure balance doesn't go below zero
                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        return new ResponseEntity<BigDecimal>(HttpStatus.BAD_REQUEST);
                    }
                    
                    user.setBalance(newBalance);
                    userRepository.save(user);
                    return ResponseEntity.ok(newBalance);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
} 