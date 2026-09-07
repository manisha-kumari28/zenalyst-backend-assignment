package com.zenalyst.backend_assignment.repository;

import com.zenalyst.backend_assignment.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}