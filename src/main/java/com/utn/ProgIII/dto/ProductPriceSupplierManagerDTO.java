package com.utn.ProgIII.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ProductPriceSupplierManagerDTO(
        @Schema(example = "1")
        Long idProductSupplier,
        @Schema(example = "1")
        Long idSupplier,
        @Schema(example = "Empresa")
        String companyName,
        @Schema(example = "100")
        BigDecimal cost,
        @Schema(example = "100")
        BigDecimal profitMargin,
        @Schema(example = "200")
        BigDecimal price,
        @Schema(example = "0.01")
        BigDecimal dollarPrice
) {

}
