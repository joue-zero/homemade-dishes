package com.dishes.dishservice.repository;

import com.dishes.dishservice.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);
    Optional<Company> findByEmail(String email);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
} 