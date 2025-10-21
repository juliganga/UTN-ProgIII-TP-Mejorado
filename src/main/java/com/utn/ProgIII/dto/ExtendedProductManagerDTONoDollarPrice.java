package com.utn.ProgIII.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ExtendedProductManagerDTONoDollarPrice(
        @Schema(example = "1")
        Long idProductSupplier,
        @Schema(example = "1")
        Long idProduct,
        @Schema(example = "Pasas de uva")
        String name,
        @Schema(example = "200")
        BigDecimal cost,
        @Schema(example = "30")
        BigDecimal profitMargin,
        @Schema(example = "260")
        BigDecimal price,
        @Schema(example = "not available")
        String dollarPrice
) {
}
