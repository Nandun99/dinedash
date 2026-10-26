package com.dinedash.delivery.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.delivery.domain.Delivery;
import com.dinedash.delivery.domain.Driver;
import com.dinedash.delivery.dto.DriverDtos.LocationRequest;
import com.dinedash.delivery.repository.DeliveryRepository;
import com.dinedash.delivery.repository.DriverRepository;
import com.dinedash.delivery.tracking.LiveTrackingPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;
    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private LiveTrackingPublisher tracking;

    @InjectMocks
    private DriverService service;

    @Test
    void busyDriverCannotGoOffline() {
        Driver driver = new Driver();
        driver.setStatus(Driver.Status.BUSY);
        when(driverRepository.findById(7L)).thenReturn(Optional.of(driver));

        assertThatThrownBy(() -> service.goOffline(7L)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void locationOfDriverOnTripIsStreamedToTracking() {
        Driver driver = new Driver();
        driver.setId(7L);
        driver.setCurrentDeliveryId("d1");
        Delivery delivery = new Delivery();
        delivery.setOrderId(100L);
        when(driverRepository.findById(7L)).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));
        when(deliveryRepository.findById("d1")).thenReturn(Optional.of(delivery));

        service.updateLocation(7L, new LocationRequest(6.9, 79.85));

        verify(tracking).publishLocation(100L, 6.9, 79.85);
    }
}
