package com.utn.ProgIII.repository;

import com.utn.ProgIII.model.Supplier.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByCompanyName(String name);
    boolean existsByCuit(String cuit);

    boolean existsByCompanyName(String companyName);

}
