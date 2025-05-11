package com.dishes.dishservice.mapper;

import com.dishes.dishservice.dto.DishDTO;
import com.dishes.dishservice.model.Dish;
import org.springframework.stereotype.Component;

@Component
public class DishMapper {
    
    public DishDTO toDTO(Dish dish) {
        if (dish == null) {
            return null;
        }

        DishDTO dto = new DishDTO();
        dto.setId(dish.getId());
        dto.setName(dish.getName());
        dto.setDescription(dish.getDescription());
        dto.setPrice(dish.getPrice());
        dto.setCategory(dish.getCategory());
        dto.setImageUrl(dish.getImageUrl());
        dto.setAvailable(dish.getAvailable());
        dto.setQuantity(dish.getQuantity());
        dto.setSellerId(dish.getSellerId());

        return dto;
    }

    public Dish toEntity(DishDTO dto) {
        if (dto == null) {
            return null;
        }

        Dish dish = new Dish();
        dish.setId(dto.getId());
        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setPrice(dto.getPrice());
        dish.setCategory(dto.getCategory());
        dish.setImageUrl(dto.getImageUrl());
        dish.setAvailable(dto.getAvailable());
        dish.setQuantity(dto.getQuantity());
        dish.setSellerId(dto.getSellerId());

        return dish;
    }
} 