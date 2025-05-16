package com.dishes.dishservice.controller;

import com.dishes.dishservice.dto.DishDTO;
import com.dishes.dishservice.mapper.DishMapper;
import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.service.ejb.DishServiceLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private DishServiceLocal dishService;

    @GetMapping
    public ResponseEntity<List<DishDTO>> getAllDishes() {
        List<Dish> dishes = dishService.getAllDishes();
        List<DishDTO> dishDTOs = dishes.stream()
                .map(dishMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dishDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DishDTO> getDish(@PathVariable Long id) {
        try {
            Dish dish = dishService.getDish(id);
            return ResponseEntity.ok(dishMapper.toDTO(dish));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<DishDTO> createDish(@RequestBody DishDTO dishDTO) {
        Dish dish = dishMapper.toEntity(dishDTO);
        Dish createdDish = dishService.createDish(dish);
        return ResponseEntity.ok(dishMapper.toDTO(createdDish));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DishDTO> updateDish(@PathVariable Long id, @RequestBody DishDTO dishDTO) {
        Dish dish = dishMapper.toEntity(dishDTO);
        dish.setId(id); // Ensure ID is set correctly
        Dish updatedDish = dishService.updateDish(dish);
        return ResponseEntity.ok(dishMapper.toDTO(updatedDish));
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<DishDTO> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        try {
            Dish updatedDish = dishService.updateStock(id, quantity);
            return ResponseEntity.ok(dishMapper.toDTO(updatedDish));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/check-stock")
    public ResponseEntity<Boolean> checkStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        boolean isAvailable = dishService.checkStock(id, quantity);
        return ResponseEntity.ok(isAvailable);
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<DishDTO> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        try {
            Dish updatedDish = dishService.updateAvailability(id, available);
            return ResponseEntity.ok(dishMapper.toDTO(updatedDish));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 
