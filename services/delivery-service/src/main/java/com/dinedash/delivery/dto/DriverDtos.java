package com.dinedash.delivery.dto;

import com.dinedash.delivery.domain.Driver;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class DriverDtos {

    private DriverDtos() {
    }

    public record ProfileRequest(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 20) String phone,
            @NotNull Driver.VehicleType vehicleType,
            @NotBlank @Size(max = 20) String vehiclePlate) {
    }

    public record LocationRequest(
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {
    }

    public record DriverResponse(Long id, String name, String phone, Driver.VehicleType vehicleType,
                                 String vehiclePlate, Driver.Status status, Double latitude, Double longitude,
                                 Instant lastSeenAt, String currentDeliveryId) {

        public static DriverResponse from(Driver d) {
            return new DriverResponse(d.getId(), d.getName(), d.getPhone(), d.getVehicleType(), d.getVehiclePlate(),
                    d.getStatus(), d.getLocation() != null ? d.getLocation().getY() : null,
                    d.getLocation() != null ? d.getLocation().getX() : null, d.getLastSeenAt(),
                    d.getCurrentDeliveryId());
        }
    }
}
