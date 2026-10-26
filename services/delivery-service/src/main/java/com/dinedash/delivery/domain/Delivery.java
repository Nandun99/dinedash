package com.dinedash.delivery.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "deliveries")
@Getter
@Setter
@NoArgsConstructor
public class Delivery {

    @Id
    private String id;

    @Indexed(unique = true)
    private Long orderId;

    private Long customerId;

    private String customerName;

    private String customerPhone;

    private String restaurantId;

    private String restaurantName;

    private GeoJsonPoint pickup;

    private GeoJsonPoint dropoff;

    private String dropoffAddress;

    private String notes;

    @Indexed
    private Status status = Status.WAITING_FOR_FOOD;

    @Indexed
    private Long driverId;

    private String driverName;

    private boolean noDriverNotified;

    private Instant createdAt;

    private Instant readyAt;

    private Instant assignedAt;

    private Instant pickedUpAt;

    private Instant deliveredAt;

    @Version
    private Long version;

    public enum Status {
        /** Paid; the kitchen is still preparing. */
        WAITING_FOR_FOOD,
        /** Food is ready; looking for a driver. */
        SEARCHING,
        ASSIGNED,
        PICKED_UP,
        DELIVERED,
        CANCELLED;

        public boolean isOpen() {
            return this != DELIVERED && this != CANCELLED;
        }
    }
}
