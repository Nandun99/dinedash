package com.dinedash.delivery.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.error.NotFoundException;
import com.dinedash.common.event.DeliveryUpdatedEvent;
import com.dinedash.common.security.CurrentUser;
import com.dinedash.common.security.Role;
import com.dinedash.delivery.domain.Delivery;
import com.dinedash.delivery.domain.Driver;
import com.dinedash.delivery.repository.DeliveryRepository;
import com.dinedash.delivery.repository.DriverRepository;
import com.dinedash.delivery.tracking.LiveTrackingPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private DeliveryEvents events;
    @Mock
    private LiveTrackingPublisher tracking;

    @InjectMocks
    private DeliveryService service;

    @Test
    void driverPicksUpAssignedDelivery() {
        Delivery d = delivery(Delivery.Status.ASSIGNED, 7L);

        var response = service.pickUp(7L, "d1");

        assertThat(response.status()).isEqualTo(Delivery.Status.PICKED_UP);
        verify(events).publish(d, DeliveryUpdatedEvent.Status.PICKED_UP);
    }

    @Test
    void completingFreesTheDriver() {
        delivery(Delivery.Status.PICKED_UP, 7L);
        Driver driver = new Driver();
        driver.setId(7L);
        driver.setStatus(Driver.Status.BUSY);
        driver.setCurrentDeliveryId("d1");
        when(driverRepository.findById(7L)).thenReturn(Optional.of(driver));

        service.complete(7L, "d1");

        assertThat(driver.getStatus()).isEqualTo(Driver.Status.AVAILABLE);
        assertThat(driver.getCurrentDeliveryId()).isNull();
    }

    @Test
    void cannotCompleteBeforePickup() {
        delivery(Delivery.Status.ASSIGNED, 7L);

        assertThatThrownBy(() -> service.complete(7L, "d1")).isInstanceOf(BadRequestException.class);
    }

    @Test
    void otherDriversCannotTouchTheDelivery() {
        when(deliveryRepository.findById("d1")).thenReturn(Optional.of(newDelivery(Delivery.Status.ASSIGNED, 7L)));

        assertThatThrownBy(() -> service.pickUp(8L, "d1")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void customerCanOnlyTrackOwnOrder() {
        Delivery d = newDelivery(Delivery.Status.ASSIGNED, 7L);
        d.setCustomerId(10L);
        when(deliveryRepository.findByOrderId(100L)).thenReturn(Optional.of(d));

        assertThatThrownBy(() -> service.forOrder(new CurrentUser(11L, null, Role.CUSTOMER), 100L))
                .isInstanceOf(NotFoundException.class);
    }

    private Delivery delivery(Delivery.Status status, Long driverId) {
        Delivery d = newDelivery(status, driverId);
        when(deliveryRepository.findById("d1")).thenReturn(Optional.of(d));
        lenient().when(deliveryRepository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));
        return d;
    }

    private static Delivery newDelivery(Delivery.Status status, Long driverId) {
        Delivery d = new Delivery();
        d.setId("d1");
        d.setOrderId(100L);
        d.setStatus(status);
        d.setDriverId(driverId);
        d.setPickup(new GeoJsonPoint(79.86, 6.91));
        d.setDropoff(new GeoJsonPoint(79.85, 6.90));
        return d;
    }
}
