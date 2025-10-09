package com.utn.ProgIII.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

public record ProductPricesDTO(
        @Schema(example = "1")
        Long idProduct,
        @Schema(example = "Papas")
        String name,
        @ArraySchema(schema = @Schema(oneOf = {ProductPriceSupplierEmployeeDTO.class, ProductPriceSupplierManagerDTONoDollarPrice.class,
                ProductPriceSupplierManagerDTO.class}))
        Page<?> prices
) {
}
