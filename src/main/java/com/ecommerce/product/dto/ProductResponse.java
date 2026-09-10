package com.ecommerce.product.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        List<CategoryDto> categories,
        OffsetDateTime createdAt
) {
}
