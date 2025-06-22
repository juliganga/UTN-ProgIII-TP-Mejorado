package com.utn.ProgIII.repository;

import com.utn.ProgIII.model.Product.Product;
import com.utn.ProgIII.model.Product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Busca por igualdad exacta del Enum
    List<Product> findByStatus(ProductStatus status);

    //Busca por contener todo o parte del string
    List<Product> findByNameContaining (String name);

    boolean existsByName(String name);

    Product getByName(String name);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
}
