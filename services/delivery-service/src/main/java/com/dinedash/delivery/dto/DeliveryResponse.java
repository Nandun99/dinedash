package com.dinedash.delivery.dto;

import com.dinedash.delivery.domain.Delivery;
import com.dinedash.delivery.domain.Driver;

import java.time.Instant;

public record DeliveryResponse(
        String id,
        Long orderId,
        Delivery.Status status,
        String restaurantName,
        double pickupLat,
        double pickupLng,
        double dropoffLat,
        double dropoffLng,
        String dropoffAddress,
        String customerName,
        String customerPhone,
        String notes,
        Long driverId,
        String driverName,
        Double driverLat,
        Double driverLng,
        Instant assignedAt,
        Instant pickedUpAt,
        Instant deliveredAt
) {
    public static DeliveryResponse from(Delivery d, Driver driver) {
        boolean showDriver = driver != null && driver.getLocation() != null && d.getStatus().isOpen();
        return new DeliveryResponse(d.getId(), d.getOrderId(), d.getStatus(), d.getRestaurantName(),
                d.getPickup().getY(), d.getPickup().getX(), d.getDropoff().getY(), d.getDropoff().getX(),
                d.getDropoffAddress(), d.getCustomerName(), d.getCustomerPhone(), d.getNotes(), d.getDriverId(),
                d.getDriverName(), showDriver ? driver.getLocation().getY() : null,
                showDriver ? driver.getLocation().getX() : null, d.getAssignedAt(), d.getPickedUpAt(),
                d.getDeliveredAt());
    }
}
