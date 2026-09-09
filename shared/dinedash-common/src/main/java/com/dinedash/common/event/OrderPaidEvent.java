package com.dinedash.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Carries everything the restaurant and delivery services need, so they do not
 * have to call back into order-service.
 */
public record OrderPaidEvent(
        Long orderId,
        Long customerId,
        String customerName,
        String customerPhone,
        String restaurantId,
        String restaurantName,
        List<OrderItemLine> items,
        BigDecimal totalAmount,
        GeoPoint pickup,
        GeoPoint dropoff,
        String dropoffAddress,
        String notes,
        Instant paidAt
) {
}
