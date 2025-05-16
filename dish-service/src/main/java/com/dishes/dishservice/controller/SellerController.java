package com.dishes.dishservice.controller;

import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.service.ejb.DishServiceLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.List;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {
    private static final Logger logger = LoggerFactory.getLogger(SellerController.class);
    
    @Autowired
    private Context ejbContext;
    
    /**
     * Get DishServiceLocal EJB instance
     */
    private DishServiceLocal getDishService() throws NamingException {
        return (DishServiceLocal) ejbContext.lookup("java:global/dish-service/StatelessDishServiceBean");
    }

    @GetMapping("/{sellerId}/dishes")
    public ResponseEntity<List<Dish>> getSellerDishes(@PathVariable Long sellerId) {
        try {
            List<Dish> dishes = getDishService().getDishesBySeller(sellerId);
            return ResponseEntity.ok(dishes);
        } catch (NamingException e) {
            logger.error("Error looking up EJB: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{sellerId}/dishes")
    public ResponseEntity<Dish> createDish(
            @PathVariable Long sellerId, 
            @RequestBody Dish dish) {
        try {
            dish.setSellerId(sellerId);
            Dish createdDish = getDishService().createDish(dish);
            return ResponseEntity.ok(createdDish);
        } catch (NamingException e) {
            logger.error("Error looking up EJB: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{sellerId}/dishes/{dishId}")
    public ResponseEntity<Dish> updateDish(
            @PathVariable Long sellerId, 
            @PathVariable Long dishId, 
            @RequestBody Dish dish) {
        try {
            dish.setSellerId(sellerId);
            dish.setId(dishId); // Ensure ID is set correctly
            
            Dish updatedDish = getDishService().updateDish(dish);
            return ResponseEntity.ok(updatedDish);
        } catch (NamingException e) {
            logger.error("Error looking up EJB: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{sellerId}/dishes/{dishId}/availability")
    public ResponseEntity<Dish> updateDishAvailability(
            @PathVariable Long sellerId, 
            @PathVariable Long dishId, 
            @RequestParam boolean available) {
        try {
            Dish updatedDish = getDishService().updateAvailability(dishId, available);
            return ResponseEntity.ok(updatedDish);
        } catch (NamingException e) {
            logger.error("Error looking up EJB: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{sellerId}/dishes/{dishId}/stock")
    public ResponseEntity<Dish> updateDishStock(
            @PathVariable Long sellerId, 
            @PathVariable Long dishId, 
            @RequestParam Integer quantity) {
        try {
            Dish updatedDish = getDishService().updateStock(dishId, quantity);
            return ResponseEntity.ok(updatedDish);
        } catch (NamingException e) {
            logger.error("Error looking up EJB: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 