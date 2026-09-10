package com.ecommerce.product.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductEvent(
        String eventId,
        Long productId,
        String name,
        BigDecimal price,
        int stockQuantity,
        OffsetDateTime occurredAt
) {
    public static ProductEvent of(Long productId, String name, BigDecimal price, int stockQuantity) {
        return new ProductEvent(
                UUID.randomUUID().toString(),
                productId,
                name,
                price,
                stockQuantity,
                OffsetDateTime.now()
        );
    }

    public static ProductEvent deleted(Long productId) {
        return new ProductEvent(
                UUID.randomUUID().toString(),
                productId,
                null,
                null,
                0,
                OffsetDateTime.now()
        );
    }
}
