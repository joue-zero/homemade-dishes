package com.dishes.dishservice.controller;

import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.service.ejb.DishServiceLocal;
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
    private DishServiceLocal dishService;

    @GetMapping("/{sellerId}/dishes")
    public ResponseEntity<List<Dish>> getSellerDishes(@PathVariable Long sellerId) {
        List<Dish> dishes = dishService.getDishesBySeller(sellerId);
        return ResponseEntity.ok(dishes);
    }

    @PostMapping("/{sellerId}/dishes")
    public ResponseEntity<Dish> createDish(
            @PathVariable Long sellerId, 
            @RequestBody Dish dish) {
        dish.setSellerId(sellerId);
        Dish createdDish = dishService.createDish(dish);
        return ResponseEntity.ok(createdDish);
    }

    @PutMapping("/{sellerId}/dishes/{dishId}")
    public ResponseEntity<Dish> updateDish(
            @PathVariable Long sellerId, 
            @PathVariable Long dishId, 
            @RequestBody Dish dish) {
        dish.setSellerId(sellerId);
        dish.setId(dishId); // Ensure ID is set correctly
        
        Dish updatedDish = dishService.updateDish(dish);
        return ResponseEntity.ok(updatedDish);
    }

    @PutMapping("/{sellerId}/dishes/{dishId}/availability")
    public ResponseEntity<Dish> updateDishAvailability(
            @PathVariable Long sellerId, 
            @PathVariable Long dishId, 
            @RequestParam boolean available) {
        Dish updatedDish = dishService.updateAvailability(dishId, available);
        return ResponseEntity.ok(updatedDish);
    }
    
    @PutMapping("/{sellerId}/dishes/{dishId}/stock")
    public ResponseEntity<Dish> updateDishStock(
            @PathVariable Long sellerId, 
            @PathVariable Long dishId, 
            @RequestParam Integer quantity) {
        Dish updatedDish = dishService.updateStock(dishId, quantity);
        return ResponseEntity.ok(updatedDish);
    }
} 