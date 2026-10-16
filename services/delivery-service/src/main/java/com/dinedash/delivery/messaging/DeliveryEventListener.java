package com.dinedash.delivery.messaging;

import com.dinedash.common.event.OrderPaidEvent;
import com.dinedash.common.event.RestaurantOrderUpdatedEvent;
import com.dinedash.common.event.Topics;
import com.dinedash.delivery.domain.Delivery;
import com.dinedash.delivery.repository.DeliveryRepository;
import com.dinedash.delivery.service.DriverAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventListener {

    private final DeliveryRepository deliveryRepository;
    private final DriverAssignmentService assignmentService;

    @KafkaListener(topics = Topics.ORDER_PAID)
    public void onOrderPaid(OrderPaidEvent event) {
        if (deliveryRepository.findByOrderId(event.orderId()).isPresent()) {
            return;
        }
        Delivery d = new Delivery();
        d.setOrderId(event.orderId());
        d.setCustomerId(event.customerId());
        d.setCustomerName(event.customerName());
        d.setCustomerPhone(event.customerPhone());
        d.setRestaurantId(event.restaurantId());
        d.setRestaurantName(event.restaurantName());
        d.setPickup(new GeoJsonPoint(event.pickup().longitude(), event.pickup().latitude()));
        d.setDropoff(new GeoJsonPoint(event.dropoff().longitude(), event.dropoff().latitude()));
        d.setDropoffAddress(event.dropoffAddress());
        d.setNotes(event.notes());
        d.setCreatedAt(Instant.now());
        deliveryRepository.save(d);
        log.info("Delivery created for order {}", event.orderId());
    }

    @KafkaListener(topics = Topics.RESTAURANT_ORDER_UPDATED)
    public void onRestaurantUpdate(RestaurantOrderUpdatedEvent event) {
        if (event.status() != RestaurantOrderUpdatedEvent.Status.READY) {
            return;
        }
        deliveryRepository.findByOrderId(event.orderId()).ifPresentOrElse(d -> {
            if (d.getStatus() == Delivery.Status.WAITING_FOR_FOOD) {
                d.setStatus(Delivery.Status.SEARCHING);
                d.setReadyAt(Instant.now());
                assignmentService.tryAssign(deliveryRepository.save(d));
            }
        }, () -> {
            // order.paid not processed yet; throw so the record is retried
            throw new IllegalStateException("Delivery for order " + event.orderId() + " not found yet");
        });
    }
}
