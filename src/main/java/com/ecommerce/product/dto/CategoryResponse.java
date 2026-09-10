package com.ecommerce.product.dto;

import java.time.OffsetDateTime;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        OffsetDateTime createdAt
) {
}
