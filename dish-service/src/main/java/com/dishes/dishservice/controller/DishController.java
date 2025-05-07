package com.dishes.dishservice.controller;

import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    @Autowired
    private DishRepository dishRepository;

    @PostMapping
    public ResponseEntity<Dish> createDish(@RequestBody Dish dish) {
        return ResponseEntity.ok(dishRepository.save(dish));
    }

    @GetMapping
    public ResponseEntity<List<Dish>> getAllDishes() {
        return ResponseEntity.ok(dishRepository.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dish> updateDish(@PathVariable Long id, @RequestBody Dish dish) {
        return dishRepository.findById(id)
                .map(existingDish -> {
                    dish.setId(id);
                    return ResponseEntity.ok(dishRepository.save(dish));
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 