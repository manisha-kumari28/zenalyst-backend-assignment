package com.zenalyst.backend_assignment.controller;

import com.zenalyst.backend_assignment.model.Product;
import com.zenalyst.backend_assignment.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(
            ProductRepository productRepository) {

        this.productRepository = productRepository;
    }

    @PostMapping
    public Product createProduct(
            @RequestBody Product product) {

        if (product == null
                || product.getName() == null
                || product.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Product name is required");
        }

        return productRepository.save(product);
    }

    @GetMapping
    public List<Product> getProducts() {

        return productRepository.findAll();
    }
}