package com.utn.ProgIII.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record SupplierProductListDTO(
        @Schema(example = "1")
        Long idSupplier,
        @Schema(example = "Empresa test")
        String companyName,
        @ArraySchema(schema= @Schema(oneOf = {ProductPriceSupplierEmployeeDTO.class, ProductPriceSupplierManagerDTONoDollarPrice.class,
                ProductPriceSupplierManagerDTO.class}))
        Page<?> productsList) {
}
