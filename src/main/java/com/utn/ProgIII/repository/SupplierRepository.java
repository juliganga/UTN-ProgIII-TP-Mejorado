package com.utn.ProgIII.repository;

import com.utn.ProgIII.model.Supplier.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByCompanyName(String name);
    Page<Supplier> findByCompanyNameContaining(String name, Pageable pageable);
    boolean existsByCuit(String cuit);
    boolean existsByCompanyName(String companyName);
}
