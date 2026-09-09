package com.dinedash.common.event;

import java.time.Instant;

public record OrderCancelledEvent(
        Long orderId,
        Long customerId,
        String restaurantId,
        String reason,
        boolean refundRequired,
        Instant cancelledAt
) {
}
