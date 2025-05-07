package com.dishes.dishservice.controller;

import com.dishes.dishservice.model.Company;
import com.dishes.dishservice.model.Dish;
import com.dishes.dishservice.service.CompanyService;
import com.dishes.dishservice.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private DishService dishService;

    @PostMapping("/auth/login")
    public ResponseEntity<Company> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(companyService.authenticate(loginRequest.getEmail(), loginRequest.getPassword()));
    }

    @GetMapping("/{companyId}/dishes")
    public ResponseEntity<List<Dish>> getCompanyDishes(@PathVariable Long companyId) {
        return ResponseEntity.ok(dishService.getDishesByCompany(companyId));
    }

    @PostMapping("/{companyId}/dishes")
    public ResponseEntity<Dish> createDish(@PathVariable Long companyId, @RequestBody Dish dish) {
        dish.setCompany(companyService.getCompany(companyId));
        return ResponseEntity.ok(dishService.createDish(dish));
    }

    @PutMapping("/{companyId}/dishes/{dishId}")
    public ResponseEntity<Dish> updateDish(@PathVariable Long companyId, @PathVariable Long dishId, @RequestBody Dish dish) {
        dish.setCompany(companyService.getCompany(companyId));
        return ResponseEntity.ok(dishService.updateDish(dishId, dish));
    }

    @DeleteMapping("/{companyId}/dishes/{dishId}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long companyId, @PathVariable Long dishId) {
        dishService.deleteDish(dishId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{companyId}/dishes/{dishId}/availability")
    public ResponseEntity<Dish> updateDishAvailability(@PathVariable Long companyId, @PathVariable Long dishId, @RequestParam boolean available) {
        return ResponseEntity.ok(dishService.updateAvailability(dishId, available));
    }
}

class LoginRequest {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
} 