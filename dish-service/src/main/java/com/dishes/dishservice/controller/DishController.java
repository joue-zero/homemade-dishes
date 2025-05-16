package com.dishes.dishservice.controller;

import com.dishes.dishservice.dto.DishDTO;
import com.dishes.dishservice.mapper.DishMapper;
import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.service.ejb.DishServiceLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private Context ejbContext;
    
    /**
     * Get DishServiceLocal EJB instance
     */
    private DishServiceLocal getDishService() throws NamingException {
        return (DishServiceLocal) ejbContext.lookup("java:global/dish-service/StatelessDishServiceBean");
    }

    @GetMapping
    public ResponseEntity<List<DishDTO>> getAllDishes() {
        try {
            List<Dish> dishes = getDishService().getAllDishes();
            List<DishDTO> dishDTOs = dishes.stream()
                    .map(dishMapper::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dishDTOs);
        } catch (NamingException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<DishDTO> getDish(@PathVariable Long id) {
        try {
            Dish dish = getDishService().getDish(id);
            return ResponseEntity.ok(dishMapper.toDTO(dish));
        } catch (NamingException e) {
            return ResponseEntity.internalServerError().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<DishDTO> createDish(@RequestBody DishDTO dishDTO) {
        try {
            Dish dish = dishMapper.toEntity(dishDTO);
            Dish createdDish = getDishService().createDish(dish);
            return ResponseEntity.ok(dishMapper.toDTO(createdDish));
        } catch (NamingException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DishDTO> updateDish(@PathVariable Long id, @RequestBody DishDTO dishDTO) {
        try {
            Dish dish = dishMapper.toEntity(dishDTO);
            dish.setId(id); // Ensure ID is set correctly
            Dish updatedDish = getDishService().updateDish(dish);
            return ResponseEntity.ok(dishMapper.toDTO(updatedDish));
        } catch (NamingException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<DishDTO> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        try {
            Dish updatedDish = getDishService().updateStock(id, quantity);
            return ResponseEntity.ok(dishMapper.toDTO(updatedDish));
        } catch (NamingException e) {
            return ResponseEntity.internalServerError().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/check-stock")
    public ResponseEntity<Boolean> checkStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        try {
            boolean isAvailable = getDishService().checkStock(id, quantity);
            return ResponseEntity.ok(isAvailable);
        } catch (NamingException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<DishDTO> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        try {
            Dish updatedDish = getDishService().updateAvailability(id, available);
            return ResponseEntity.ok(dishMapper.toDTO(updatedDish));
        } catch (NamingException e) {
            return ResponseEntity.internalServerError().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 
