package com.dishes.dishservice.service;

import com.dishes.dishservice.model.Company;
import com.dishes.dishservice.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {
    private static final Logger logger = LoggerFactory.getLogger(CompanyService.class);

    @Autowired
    private CompanyRepository companyRepository;

    @Transactional
    public Company createCompany(Company company) {
        if (companyRepository.existsByName(company.getName())) {
            throw new RuntimeException("Company name already exists");
        }
        if (companyRepository.existsByEmail(company.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Generate a random password
        String generatedPassword = UUID.randomUUID().toString().substring(0, 8);
        company.setPassword(generatedPassword);

        Company savedCompany = companyRepository.save(company);
        logger.info("Created new company: {}", savedCompany.getName());
        
        // Return the company with the plain text password for admin to send to the company
        savedCompany.setPassword(generatedPassword);
        return savedCompany;
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
    }

    @Transactional
    public Company updateCompany(Long id, Company companyDetails) {
        Company company = getCompany(id);
        
        if (!company.getName().equals(companyDetails.getName()) && 
            companyRepository.existsByName(companyDetails.getName())) {
            throw new RuntimeException("Company name already exists");
        }
        
        if (!company.getEmail().equals(companyDetails.getEmail()) && 
            companyRepository.existsByEmail(companyDetails.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        company.setName(companyDetails.getName());
        company.setEmail(companyDetails.getEmail());
        company.setPhone(companyDetails.getPhone());
        company.setAddress(companyDetails.getAddress());

        return companyRepository.save(company);
    }

    @Transactional
    public void deleteCompany(Long id) {
        Company company = getCompany(id);
        companyRepository.delete(company);
    }

    public Company authenticate(String email, String password) {
        Company company = companyRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Company not found with email: " + email));

        if (!password.equals(company.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return company;
    }
} 