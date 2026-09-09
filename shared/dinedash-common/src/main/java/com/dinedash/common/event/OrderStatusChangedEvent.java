package com.dinedash.common.event;

import java.time.Instant;

public record OrderStatusChangedEvent(
        Long orderId,
        Long customerId,
        String restaurantId,
        String status,
        Long driverId,
        Instant changedAt
) {
}
