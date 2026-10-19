package com.dinedash.delivery.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.error.NotFoundException;
import com.dinedash.common.event.DeliveryUpdatedEvent;
import com.dinedash.common.security.CurrentUser;
import com.dinedash.common.security.Role;
import com.dinedash.delivery.domain.Delivery;
import com.dinedash.delivery.domain.Driver;
import com.dinedash.delivery.dto.DeliveryResponse;
import com.dinedash.delivery.repository.DeliveryRepository;
import com.dinedash.delivery.repository.DriverRepository;
import com.dinedash.delivery.tracking.LiveTrackingPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DriverRepository driverRepository;
    private final DeliveryEvents events;
    private final LiveTrackingPublisher tracking;

    public Optional<DeliveryResponse> currentForDriver(Long driverId) {
        Driver driver = driverRepository.findById(driverId).orElse(null);
        if (driver == null || driver.getCurrentDeliveryId() == null) {
            return Optional.empty();
        }
        return deliveryRepository.findById(driver.getCurrentDeliveryId()).map(d -> DeliveryResponse.from(d, driver));
    }

    public List<DeliveryResponse> historyForDriver(Long driverId) {
        return deliveryRepository.findTop20ByDriverIdOrderByAssignedAtDesc(driverId).stream()
                .map(d -> DeliveryResponse.from(d, null))
                .toList();
    }

    public DeliveryResponse pickUp(Long driverId, String deliveryId) {
        Delivery d = ownedByDriver(driverId, deliveryId);
        if (d.getStatus() != Delivery.Status.ASSIGNED) {
            throw new BadRequestException("This delivery is " + d.getStatus());
        }
        d.setStatus(Delivery.Status.PICKED_UP);
        d.setPickedUpAt(Instant.now());
        return save(d, DeliveryUpdatedEvent.Status.PICKED_UP);
    }

    public DeliveryResponse complete(Long driverId, String deliveryId) {
        Delivery d = ownedByDriver(driverId, deliveryId);
        if (d.getStatus() != Delivery.Status.PICKED_UP) {
            throw new BadRequestException("Pick up the order before completing it");
        }
        d.setStatus(Delivery.Status.DELIVERED);
        d.setDeliveredAt(Instant.now());
        releaseDriver(driverId, Driver.Status.AVAILABLE);
        return save(d, DeliveryUpdatedEvent.Status.DELIVERED);
    }

    public void cancelForOrder(Long orderId) {
        deliveryRepository.findByOrderId(orderId).ifPresent(d -> {
            if (!d.getStatus().isOpen()) {
                return;
            }
            d.setStatus(Delivery.Status.CANCELLED);
            if (d.getDriverId() != null) {
                releaseDriver(d.getDriverId(), Driver.Status.AVAILABLE);
            }
            deliveryRepository.save(d);
            tracking.publishStatus(d);
            log.info("Delivery for order {} cancelled", orderId);
        });
    }

    /** Customers (and admins) can see the delivery of their order, including the driver's location. */
    public DeliveryResponse forOrder(CurrentUser user, Long orderId) {
        Delivery d = deliveryRepository.findByOrderId(orderId)
                .filter(x -> user.is(Role.ADMIN) || Objects.equals(x.getCustomerId(), user.id())
                        || Objects.equals(x.getDriverId(), user.id()))
                .orElseThrow(() -> new NotFoundException("Delivery not found"));
        Driver driver = d.getDriverId() != null ? driverRepository.findById(d.getDriverId()).orElse(null) : null;
        return DeliveryResponse.from(d, driver);
    }

    private Delivery ownedByDriver(Long driverId, String deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .filter(d -> Objects.equals(d.getDriverId(), driverId))
                .orElseThrow(() -> new NotFoundException("Delivery not found"));
    }

    private void releaseDriver(Long driverId, Driver.Status status) {
        driverRepository.findById(driverId).ifPresent(driver -> {
            driver.setStatus(status);
            driver.setCurrentDeliveryId(null);
            driverRepository.save(driver);
        });
    }

    private DeliveryResponse save(Delivery d, DeliveryUpdatedEvent.Status status) {
        Delivery saved = deliveryRepository.save(d);
        events.publish(saved, status);
        tracking.publishStatus(saved);
        log.info("Order {} delivery {}", saved.getOrderId(), status);
        return DeliveryResponse.from(saved, null);
    }
}
