package com.dishes.dishservice.service.ejb;

import com.dishes.dishservice.model.Dish;
import jakarta.ejb.Local;

import java.util.List;
import java.util.Set;

/**
 * Local interface for Stateful EJB to track user's dish browsing session
 */
@Local
public interface DishSessionLocal {
    void addViewedDish(Long dishId);
    void addToFavorites(Long dishId);
    void removeFromFavorites(Long dishId);
    Set<Long> getViewedDishes();
    Set<Long> getFavoriteDishes();
    List<Dish> getRecentlyViewedDishes();
    List<Dish> getFavoriteDishDetails();
    void endSession();
} 