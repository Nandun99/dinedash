package com.dinedash.common.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        Long orderId,
        Long customerId,
        String restaurantId,
        BigDecimal totalAmount,
        String currency,
        Instant createdAt
) {
}
