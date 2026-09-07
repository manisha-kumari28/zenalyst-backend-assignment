package com.zenalyst.backend_assignment.controller;

import com.zenalyst.backend_assignment.model.SupplierOffer;
import com.zenalyst.backend_assignment.repository.ProductRepository;
import com.zenalyst.backend_assignment.repository.SupplierOfferRepository;
import com.zenalyst.backend_assignment.repository.SupplierRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class SupplierOfferController {

    private final SupplierOfferRepository supplierOfferRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public SupplierOfferController(
            SupplierOfferRepository supplierOfferRepository,
            ProductRepository productRepository,
            SupplierRepository supplierRepository) {

        this.supplierOfferRepository = supplierOfferRepository;
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
    }

    @PostMapping
    public SupplierOffer createOffer(
            @RequestBody SupplierOffer offer) {

        validateOffer(offer);

        return supplierOfferRepository.save(offer);
    }

    @GetMapping
    public List<SupplierOffer> getOffers() {

        return supplierOfferRepository.findAll();
    }

    @GetMapping("/product/{productId}")
    public List<SupplierOffer> getOffersByProduct(
            @PathVariable Long productId) {

        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException(
                    "Product not found: " + productId);
        }

        return supplierOfferRepository
                .findByProductId(productId);
    }

    private void validateOffer(SupplierOffer offer) {

        if (offer == null) {
            throw new IllegalArgumentException(
                    "Offer is required");
        }

        if (offer.getProductId() == null) {
            throw new IllegalArgumentException(
                    "Product ID is required");
        }

        if (!productRepository.existsById(
                offer.getProductId())) {

            throw new IllegalArgumentException(
                    "Product not found: "
                            + offer.getProductId());
        }

        if (offer.getSupplierId() == null) {
            throw new IllegalArgumentException(
                    "Supplier ID is required");
        }

        if (!supplierRepository.existsById(
                offer.getSupplierId())) {

            throw new IllegalArgumentException(
                    "Supplier not found: "
                            + offer.getSupplierId());
        }

        if (offer.getPrice() <= 0) {
            throw new IllegalArgumentException(
                    "Price must be greater than zero");
        }

        if (offer.getMinimumQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "Minimum quantity must be greater than zero");
        }

        if (offer.getMaximumQuantity()
                < offer.getMinimumQuantity()) {

            throw new IllegalArgumentException(
                    "Maximum quantity must be greater than "
                            + "or equal to minimum quantity");
        }
    }
}