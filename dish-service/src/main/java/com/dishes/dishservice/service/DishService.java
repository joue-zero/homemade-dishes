package com.dishes.dishservice.service;

import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.repository.DishRepository;
import com.dishes.dishservice.service.ejb.DishServiceLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Standard Spring service implementation of DishServiceLocal
 */
@Service
public class DishService implements DishServiceLocal {
    private static final Logger logger = LoggerFactory.getLogger(DishService.class);

    @Autowired
    private DishRepository dishRepository;

    @Override
    public List<Dish> getAllDishes() {
        return dishRepository.findAll();
    }

    @Override
    public Dish getDish(Long id) {
        return dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dish not found"));
    }

    @Override
    public List<Dish> getDishesBySeller(Long sellerId) {
        return dishRepository.findBySellerId(sellerId);
    }

    @Override
    public Dish createDish(Dish dish) {
        return dishRepository.save(dish);
    }

    @Override
    @Transactional
    public Dish updateDish(Dish dish) {
        return dishRepository.save(dish);
    }

    @Override
    @Transactional
    public Dish updateStock(Long id, Integer quantity) {
        Dish dish = getDish(id);
        dish.setQuantity(quantity);
        
        // If quantity is zero or negative, mark as unavailable
        if (quantity <= 0) {
            dish.setAvailable(false);
        }
        
        return dishRepository.save(dish);
    }

    @Override
    @Transactional
    public boolean checkStock(Long id, Integer requestedQuantity) {
        Dish dish = dishRepository.findById(id).orElse(null);
        
        if (dish == null) {
            logger.error("Dish not found with ID: {}", id);
            return false;
        }
        
        if (!dish.getAvailable()) {
            logger.error("Dish is not available: {}", dish.getName());
            return false;
        }
        
        if (dish.getQuantity() == null || dish.getQuantity() < requestedQuantity) {
            logger.error("Insufficient stock for dish: {}. Requested: {}, Available: {}", 
                dish.getName(), requestedQuantity, dish.getQuantity());
            return false;
        }
        
        return true;
    }

    @Override
    public Dish updateAvailability(Long id, boolean available) {
        Dish dish = getDish(id);
        dish.setAvailable(available);
        return dishRepository.save(dish);
    }
} 