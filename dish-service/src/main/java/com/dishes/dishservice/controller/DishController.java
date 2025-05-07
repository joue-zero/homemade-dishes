package com.dishes.dishservice.controller;

import com.dishes.dishservice.dto.DishDTO;
import com.dishes.dishservice.mapper.DishMapper;
import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishMapper dishMapper;

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
        Dish dish = dishService.getDish(id);
        return ResponseEntity.ok(dishMapper.toDTO(dish));
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
        Dish updatedDish = dishService.updateDish(id, dish);
        return ResponseEntity.ok(dishMapper.toDTO(updatedDish));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<DishDTO> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        Dish updatedDish = dishService.updateAvailability(id, available);
        return ResponseEntity.ok(dishMapper.toDTO(updatedDish));
    }
} 
