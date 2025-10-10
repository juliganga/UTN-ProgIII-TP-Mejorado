package com.utn.ProgIII.service.interfaces;

import com.utn.ProgIII.dto.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.math.BigDecimal;

public interface ProductSupplierService {

    ResponseProductSupplierDTO createProductSupplier(CreateProductSupplierDTO createProductSupplierDTO);
    ResponseProductSupplierDTO updateProductSupplier(UpdateProductSupplierDTO updateProductSupplierDTO, Long id);
    SupplierProductListDTO listProductsBySupplier(Pageable pageable, Long companyName, String exchange_rate);
    ProductPricesDTO listPricesByProduct(Pageable pageable, Long idProduct, String exchange_rate);
    String uploadCsv(String filepath, Long idSupplier);
    String uploadCsv(String filepath, Long idSupplier, BigDecimal bulkProfitMargin);
}
