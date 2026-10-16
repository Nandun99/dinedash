package com.dinedash.delivery.tracking;

import com.dinedash.delivery.domain.Delivery;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FirebaseTrackingPublisher implements LiveTrackingPublisher {

    private final FirebaseDatabase database;

    public FirebaseTrackingPublisher(FirebaseDatabase database) {
        this.database = database;
    }

    @Override
    public void publishLocation(Long orderId, double latitude, double longitude) {
        Map<String, Object> location = Map.of(
                "lat", latitude,
                "lng", longitude,
                "updatedAt", System.currentTimeMillis());
        ref(orderId).child("driverLocation").setValueAsync(location);
    }

    @Override
    public void publishStatus(Delivery delivery) {
        Map<String, Object> status = new HashMap<>();
        status.put("status", delivery.getStatus().name());
        status.put("driverName", delivery.getDriverName());
        status.put("updatedAt", System.currentTimeMillis());
        ref(delivery.getOrderId()).updateChildrenAsync(status);
        if (!delivery.getStatus().isOpen()) {
            // Stop exposing the driver's location once the trip is over
            ref(delivery.getOrderId()).child("driverLocation").removeValueAsync();
        }
    }

    private DatabaseReference ref(Long orderId) {
        return database.getReference("deliveries").child(String.valueOf(orderId));
    }
}
