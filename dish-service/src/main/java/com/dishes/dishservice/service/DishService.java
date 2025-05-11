package com.dishes.dishservice.service;

import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class DishService {
    private static final Logger logger = LoggerFactory.getLogger(DishService.class);

    @Autowired
    private DishRepository dishRepository;

    public List<Dish> getAllDishes() {
        return dishRepository.findAll();
    }

    public Dish getDish(Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dish not found"));
    }

    public List<Dish> getDishesBySeller(Long sellerId) {
        return dishRepository.findBySellerId(sellerId);
    }

    public Dish createDish(Dish dish) {
        logger.info("Creating dish: {}", dish);
        return dishRepository.save(dish);
    }

    public Dish updateDish(Long id, Dish dish) {
        logger.info("Updating dish with id {}: {}", id, dish);
        
        // Log incoming values for critical fields
        logger.info("Incoming values - category: '{}', quantity: {}", 
                dish.getCategory(), dish.getQuantity());
        
        Dish existingDish = getDish(id);
        logger.info("Existing dish before update: {}", existingDish);
        logger.info("Existing values - category: '{}', quantity: {}", 
                existingDish.getCategory(), existingDish.getQuantity());
        
        // Update all fields
        existingDish.setName(dish.getName());
        existingDish.setDescription(dish.getDescription());
        existingDish.setPrice(dish.getPrice());
        existingDish.setCategory(dish.getCategory());
        existingDish.setImageUrl(dish.getImageUrl());
        existingDish.setAvailable(dish.getAvailable());
        existingDish.setQuantity(dish.getQuantity());
        existingDish.setSellerId(dish.getSellerId());
        
        // Log values after setting but before save
        logger.info("Updated dish before save: {}", existingDish);
        logger.info("Updated values before save - category: '{}', quantity: {}", 
                existingDish.getCategory(), existingDish.getQuantity());
        
        // Save the dish
        Dish updatedDish = dishRepository.save(existingDish);
        
        // Log the final result after save
        logger.info("Dish after save: {}", updatedDish);
        logger.info("Final values - category: '{}', quantity: {}", 
                updatedDish.getCategory(), updatedDish.getQuantity());
        
        return updatedDish;
    }

    public void deleteDish(Long id) {
        dishRepository.deleteById(id);
    }

    public Dish updateAvailability(Long id, boolean available) {
        Dish dish = getDish(id);
        dish.setAvailable(available);
        return dishRepository.save(dish);
    }
} 