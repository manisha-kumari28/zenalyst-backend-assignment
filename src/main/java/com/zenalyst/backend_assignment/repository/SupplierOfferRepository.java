package com.zenalyst.backend_assignment.repository;

import com.zenalyst.backend_assignment.model.SupplierOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupplierOfferRepository
        extends JpaRepository<SupplierOffer, Long> {

    List<SupplierOffer> findByProductId(Long productId);
}