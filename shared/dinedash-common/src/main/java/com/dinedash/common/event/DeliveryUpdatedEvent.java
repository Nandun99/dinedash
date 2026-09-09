package com.dinedash.common.event;

import java.time.Instant;

public record DeliveryUpdatedEvent(
        Long orderId,
        String deliveryId,
        Long driverId,
        String driverName,
        Status status,
        Instant updatedAt
) {
    public enum Status {
        ASSIGNED,
        PICKED_UP,
        DELIVERED,
        NO_DRIVER_AVAILABLE
    }
}
