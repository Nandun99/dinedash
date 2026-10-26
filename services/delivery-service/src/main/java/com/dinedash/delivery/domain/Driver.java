package com.dinedash.delivery.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/** Keyed by the driver's user id from auth-user-service. */
@Document(collection = "drivers")
@Getter
@Setter
@NoArgsConstructor
public class Driver {

    @Id
    private Long id;

    private String name;

    private String phone;

    private VehicleType vehicleType;

    private String vehiclePlate;

    @Indexed
    private Status status = Status.OFFLINE;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;

    private Instant lastSeenAt;

    private String currentDeliveryId;

    @Version
    private Long version;

    public enum Status {
        OFFLINE, AVAILABLE, BUSY
    }

    public enum VehicleType {
        BICYCLE, MOTORBIKE, CAR
    }
}
