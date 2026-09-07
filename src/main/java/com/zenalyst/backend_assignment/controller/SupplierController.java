package com.zenalyst.backend_assignment.controller;

import com.zenalyst.backend_assignment.model.Supplier;
import com.zenalyst.backend_assignment.repository.SupplierRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierRepository supplierRepository;

    public SupplierController(
            SupplierRepository supplierRepository) {

        this.supplierRepository = supplierRepository;
    }

    @PostMapping
    public Supplier createSupplier(
            @RequestBody Supplier supplier) {

        validateSupplier(supplier);

        return supplierRepository.save(supplier);
    }

    @GetMapping
    public List<Supplier> getSuppliers() {

        return supplierRepository.findAll();
    }

    private void validateSupplier(
            Supplier supplier) {

        if (supplier == null) {
            throw new IllegalArgumentException(
                    "Supplier is required");
        }

        if (supplier.getName() == null
                || supplier.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Supplier name is required");
        }

        if (supplier.getDeliveryDays() < 0) {

            throw new IllegalArgumentException(
                    "Delivery days cannot be negative");
        }

        if (supplier.getMinimumOrderQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "Minimum order quantity must be greater than zero");
        }

        if (supplier.getMaximumSupplyQuantity() <= 0) {

            throw new IllegalArgumentException(
                    "Maximum supply quantity must be greater than zero");
        }

        if (supplier.getMaximumSupplyQuantity()
                < supplier.getMinimumOrderQuantity()) {

            throw new IllegalArgumentException(
                    "Maximum supply quantity must be greater than "
                            + "or equal to minimum order quantity");
        }
    }
}