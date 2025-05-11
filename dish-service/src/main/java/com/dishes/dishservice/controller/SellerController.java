package com.dishes.dishservice.controller;

import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    @Autowired
    private DishService dishService;

    @GetMapping("/{sellerId}/dishes")
    public ResponseEntity<List<Dish>> getSellerDishes(@PathVariable Long sellerId) {
        return ResponseEntity.ok(dishService.getDishesBySeller(sellerId));
    }

    @PostMapping("/{sellerId}/dishes")
    public ResponseEntity<Dish> createDish(@PathVariable Long sellerId, @RequestBody Dish dish) {
        dish.setSellerId(sellerId);
        return ResponseEntity.ok(dishService.createDish(dish));
    }

    @PutMapping("/{sellerId}/dishes/{dishId}")
    public ResponseEntity<Dish> updateDish(@PathVariable Long sellerId, @PathVariable Long dishId, @RequestBody Dish dish) {
        dish.setSellerId(sellerId);
        return ResponseEntity.ok(dishService.updateDish(dishId, dish));
    }

    @DeleteMapping("/{sellerId}/dishes/{dishId}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long sellerId, @PathVariable Long dishId) {
        dishService.deleteDish(dishId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{sellerId}/dishes/{dishId}/availability")
    public ResponseEntity<Dish> updateDishAvailability(@PathVariable Long sellerId, @PathVariable Long dishId, @RequestParam boolean available) {
        return ResponseEntity.ok(dishService.updateAvailability(dishId, available));
    }
} 