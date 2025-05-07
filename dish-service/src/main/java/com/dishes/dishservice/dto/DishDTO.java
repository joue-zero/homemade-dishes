package com.dishes.dishservice.dto;

import lombok.Data;

@Data
public class DishDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String imageUrl;
    private Boolean available;
    private CompanyDTO company;

    @Data
    public static class CompanyDTO {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String address;
    }
} 