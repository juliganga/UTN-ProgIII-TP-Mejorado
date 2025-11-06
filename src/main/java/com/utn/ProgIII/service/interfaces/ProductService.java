package com.utn.ProgIII.service.interfaces;

import com.utn.ProgIII.dto.ProductDTO;
import com.utn.ProgIII.model.Product.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    ProductDTO getProductById(Long id);
    Page<ProductDTO> getAllProduct (Pageable paginacion);
    List<ProductDTO> getAllActiveProductAsList();
    Page<ProductDTO> getAllProductByStatus (String status,Pageable pageable);
    List<ProductDTO> listProductNames();
    Page<ProductDTO> getProductByName(String name,Pageable pageable);
    ProductDTO createProductDto (ProductDTO prductoDto);
    ProductDTO updateProduct (Long id, ProductDTO productDto);
    void deleteProduct (Long id);

}
