package com.dishes.dishservice.service.ejb;

import com.dishes.dishservice.model.Dish;
import jakarta.ejb.Local;

import java.util.List;

/**
 * Local EJB interface for Dish Service operations
 */
@Local
public interface DishServiceLocal {
    List<Dish> getAllDishes();
    Dish getDish(Long id);
    List<Dish> getDishesBySeller(Long sellerId);
    Dish createDish(Dish dish);
    Dish updateDish(Dish dish);
    Dish updateStock(Long id, Integer quantity);
    boolean checkStock(Long id, Integer requestedQuantity);
    Dish updateAvailability(Long id, boolean available);
} 