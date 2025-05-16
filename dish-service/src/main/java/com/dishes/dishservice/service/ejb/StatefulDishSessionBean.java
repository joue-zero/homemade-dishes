package com.dishes.dishservice.service.ejb;

import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.repository.DishRepository;
import jakarta.ejb.Remove;
import jakarta.ejb.Stateful;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Stateful Session Bean implementation to track user's dish browsing session
 */
@Stateful
public class StatefulDishSessionBean implements DishSessionLocal, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(StatefulDishSessionBean.class);
    
    // Maximum number of viewed dishes to track
    private static final int MAX_VIEWED_DISHES = 10;

    private final Set<Long> viewedDishes = new LinkedHashSet<>();
    private final Set<Long> favoriteDishes = new HashSet<>();

    @Inject
    private DishRepository dishRepository;

    @Override
    public void addViewedDish(Long dishId) {
        // If we've reached max capacity, remove the oldest viewed dish
        if (viewedDishes.size() >= MAX_VIEWED_DISHES) {
            // Remove the oldest dish (first in the LinkedHashSet)
            Iterator<Long> iterator = viewedDishes.iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
        
        // Add the new dish to the viewed list
        viewedDishes.add(dishId);
        logger.info("Added dish ID {} to viewed dishes. Total viewed: {}", dishId, viewedDishes.size());
    }

    @Override
    public void addToFavorites(Long dishId) {
        favoriteDishes.add(dishId);
        logger.info("Added dish ID {} to favorites. Total favorites: {}", dishId, favoriteDishes.size());
    }

    @Override
    public void removeFromFavorites(Long dishId) {
        favoriteDishes.remove(dishId);
        logger.info("Removed dish ID {} from favorites. Total favorites: {}", dishId, favoriteDishes.size());
    }

    @Override
    public Set<Long> getViewedDishes() {
        return new LinkedHashSet<>(viewedDishes);
    }

    @Override
    public Set<Long> getFavoriteDishes() {
        return new HashSet<>(favoriteDishes);
    }

    @Override
    public List<Dish> getRecentlyViewedDishes() {
        if (viewedDishes.isEmpty()) {
            return Collections.emptyList();
        }
        
        return dishRepository.findAllById(viewedDishes);
    }

    @Override
    public List<Dish> getFavoriteDishDetails() {
        if (favoriteDishes.isEmpty()) {
            return Collections.emptyList();
        }
        
        return dishRepository.findAllById(favoriteDishes);
    }

    @Override
    @Remove
    public void endSession() {
        logger.info("Ending user session. Viewed {} dishes and had {} favorites.",
                viewedDishes.size(), favoriteDishes.size());
        viewedDishes.clear();
        favoriteDishes.clear();
    }
} 