package com.dishes.dishservice.controller;

import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.service.DishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {
    private static final Logger logger = LoggerFactory.getLogger(SellerController.class);
    
    @Autowired
    private DishService dishService;

    @GetMapping("/{sellerId}/dishes")
    public ResponseEntity<List<Dish>> getSellerDishes(@PathVariable Long sellerId) {
        logger.info("Fetching dishes for seller: {}", sellerId);
        List<Dish> dishes = dishService.getDishesBySeller(sellerId);
        logger.info("Found {} dishes for seller: {}", dishes.size(), sellerId);
        return ResponseEntity.ok(dishes);
    }

    @PostMapping("/{sellerId}/dishes")
    public ResponseEntity<Dish> createDish(
            @PathVariable Long sellerId, 
            @RequestBody Dish dish) {
        logger.info("Creating dish for seller: {}", sellerId);
        logger.info("Received dish: {}", dish);

        dish.setSellerId(sellerId);
        Dish createdDish = dishService.createDish(dish);
        logger.info("Dish created successfully: {}", createdDish);
        
        return ResponseEntity.ok(createdDish);
    }

    @PutMapping("/{sellerId}/dishes/{dishId}")
    public ResponseEntity<Dish> updateDish(
            @PathVariable Long sellerId, 
            @PathVariable Long dishId, 
            @RequestBody Dish dish) {
        logger.info("Updating dish {} for seller {}", dishId, sellerId);
        logger.info("Received dish object: {}", dish);
        
        dish.setSellerId(sellerId);
        dish.setId(dishId); // Ensure ID is set correctly
        
        Dish updatedDish = dishService.updateDish(dish);
        logger.info("Dish updated successfully: {}", updatedDish);
        
        return ResponseEntity.ok(updatedDish);
    }

    @PutMapping("/{sellerId}/dishes/{dishId}/availability")
    public ResponseEntity<Dish> updateDishAvailability(
            @PathVariable Long sellerId, 
            @PathVariable Long dishId, 
            @RequestParam boolean available) {
        logger.info("Updating availability of dish {} for seller {} to: {}", dishId, sellerId, available);
        Dish updatedDish = dishService.updateAvailability(dishId, available);
        logger.info("Dish availability updated successfully: {}", updatedDish);
        return ResponseEntity.ok(updatedDish);
    }
    
    @PutMapping("/{sellerId}/dishes/{dishId}/stock")
    public ResponseEntity<Dish> updateDishStock(
            @PathVariable Long sellerId, 
            @PathVariable Long dishId, 
            @RequestParam Integer quantity) {
        logger.info("Updating stock of dish {} for seller {} to: {}", dishId, sellerId, quantity);
        Dish updatedDish = dishService.updateStock(dishId, quantity);
        logger.info("Dish stock updated successfully: {}", updatedDish);
        return ResponseEntity.ok(updatedDish);
    }
} 