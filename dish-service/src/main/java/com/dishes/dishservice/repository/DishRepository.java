package com.dishes.dishservice.repository;

import com.dishes.dishservice.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishRepository extends JpaRepository<Dish, Long> {
} 