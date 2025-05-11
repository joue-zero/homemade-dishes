package com.dishes.dishservice.service;

import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishService {

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
        return dishRepository.save(dish);
    }

    public Dish updateDish(Long id, Dish dish) {
        Dish existingDish = getDish(id);
        existingDish.setName(dish.getName());
        existingDish.setDescription(dish.getDescription());
        existingDish.setPrice(dish.getPrice());
        existingDish.setAvailable(dish.getAvailable());
        existingDish.setSellerId(dish.getSellerId());
        return dishRepository.save(existingDish);
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