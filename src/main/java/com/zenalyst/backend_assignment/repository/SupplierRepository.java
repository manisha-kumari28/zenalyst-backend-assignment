package com.zenalyst.backend_assignment.repository;

import com.zenalyst.backend_assignment.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}