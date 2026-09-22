package com.dinedash.order.dto;

import com.dinedash.order.domain.Order;
import com.dinedash.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String restaurantId,
        String restaurantName,
        OrderStatus status,
        List<Item> items,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal total,
        String currency,
        String deliveryAddress,
        double dropoffLat,
        double dropoffLng,
        double pickupLat,
        double pickupLng,
        String notes,
        Long driverId,
        String driverName,
        String cancelReason,
        Instant placedAt,
        Instant updatedAt,
        List<StatusEntry> history
) {
    public record Item(String menuItemId, String name, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
    }

    public record StatusEntry(OrderStatus status, String note, Instant at) {
    }

    public static OrderResponse from(Order o) {
        return new OrderResponse(o.getId(), o.getRestaurantId(), o.getRestaurantName(), o.getStatus(),
                o.getItems().stream()
                        .map(i -> new Item(i.getMenuItemId(), i.getName(), i.getUnitPrice(), i.getQuantity(), i.lineTotal()))
                        .toList(),
                o.getSubtotal(), o.getDeliveryFee(), o.getTotal(), o.getCurrency(), o.getDeliveryAddress(),
                o.getDropoffLat(), o.getDropoffLng(), o.getPickupLat(), o.getPickupLng(), o.getNotes(),
                o.getDriverId(), o.getDriverName(), o.getCancelReason(), o.getPlacedAt(), o.getUpdatedAt(),
                o.getHistory().stream().map(h -> new StatusEntry(h.getStatus(), h.getNote(), h.getChangedAt())).toList());
    }
}
