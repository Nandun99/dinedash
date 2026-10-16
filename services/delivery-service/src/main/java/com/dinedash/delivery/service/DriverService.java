package com.dinedash.delivery.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.error.NotFoundException;
import com.dinedash.delivery.domain.Driver;
import com.dinedash.delivery.dto.DriverDtos.DriverResponse;
import com.dinedash.delivery.dto.DriverDtos.LocationRequest;
import com.dinedash.delivery.dto.DriverDtos.ProfileRequest;
import com.dinedash.delivery.repository.DeliveryRepository;
import com.dinedash.delivery.repository.DriverRepository;
import com.dinedash.delivery.tracking.LiveTrackingPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final DeliveryRepository deliveryRepository;
    private final LiveTrackingPublisher tracking;

    public DriverResponse me(Long driverId) {
        return DriverResponse.from(find(driverId));
    }

    public DriverResponse saveProfile(Long driverId, ProfileRequest request) {
        Driver driver = driverRepository.findById(driverId).orElseGet(() -> {
            Driver d = new Driver();
            d.setId(driverId);
            return d;
        });
        driver.setName(request.name().trim());
        driver.setPhone(request.phone());
        driver.setVehicleType(request.vehicleType());
        driver.setVehiclePlate(request.vehiclePlate().trim().toUpperCase());
        return DriverResponse.from(driverRepository.save(driver));
    }

    public DriverResponse goOnline(Long driverId, LocationRequest location) {
        Driver driver = find(driverId);
        if (driver.getStatus() == Driver.Status.BUSY) {
            return DriverResponse.from(driver);
        }
        applyLocation(driver, location);
        driver.setStatus(Driver.Status.AVAILABLE);
        log.info("Driver {} is online", driverId);
        return DriverResponse.from(driverRepository.save(driver));
    }

    public DriverResponse goOffline(Long driverId) {
        Driver driver = find(driverId);
        if (driver.getStatus() == Driver.Status.BUSY) {
            throw new BadRequestException("Finish your current delivery before going offline");
        }
        driver.setStatus(Driver.Status.OFFLINE);
        return DriverResponse.from(driverRepository.save(driver));
    }

    public Driver updateLocation(Long driverId, LocationRequest location) {
        Driver driver = find(driverId);
        applyLocation(driver, location);
        Driver saved = driverRepository.save(driver);
        if (saved.getCurrentDeliveryId() != null) {
            deliveryRepository.findById(saved.getCurrentDeliveryId()).ifPresent(d ->
                    tracking.publishLocation(d.getOrderId(), location.latitude(), location.longitude()));
        }
        return saved;
    }

    public Driver find(Long driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new NotFoundException("Set up your driver profile first"));
    }

    private static void applyLocation(Driver driver, LocationRequest location) {
        driver.setLocation(new GeoJsonPoint(location.longitude(), location.latitude()));
        driver.setLastSeenAt(Instant.now());
    }
}
