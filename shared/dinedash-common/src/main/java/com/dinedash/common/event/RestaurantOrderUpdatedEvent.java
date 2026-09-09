package com.dinedash.common.event;

import java.time.Instant;

public record RestaurantOrderUpdatedEvent(
        Long orderId,
        String restaurantId,
        Status status,
        String reason,
        Integer prepMinutes,
        Instant updatedAt
) {
    public enum Status {
        ACCEPTED,
        REJECTED,
        READY
    }
}
