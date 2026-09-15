package com.dinedash.order.messaging;

import com.dinedash.common.event.DeliveryUpdatedEvent;
import com.dinedash.common.event.PaymentCompletedEvent;
import com.dinedash.common.event.PaymentFailedEvent;
import com.dinedash.common.event.RestaurantOrderUpdatedEvent;
import com.dinedash.common.event.Topics;
import com.dinedash.order.domain.InvalidStatusTransitionException;
import com.dinedash.order.service.OrderLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Reacts to other services. An out-of-order or stale event (e.g. READY for a cancelled
 * order) is logged and skipped instead of being retried forever.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderLifecycleService lifecycle;

    @KafkaListener(topics = Topics.PAYMENT_COMPLETED)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        safely(event.orderId(), () -> lifecycle.onPaymentCompleted(event.orderId()));
    }

    @KafkaListener(topics = Topics.PAYMENT_FAILED)
    public void onPaymentFailed(PaymentFailedEvent event) {
        safely(event.orderId(), () -> lifecycle.onPaymentFailed(event.orderId(), event.reason()));
    }

    @KafkaListener(topics = Topics.RESTAURANT_ORDER_UPDATED)
    public void onRestaurantUpdate(RestaurantOrderUpdatedEvent event) {
        safely(event.orderId(), () -> {
            switch (event.status()) {
                case ACCEPTED -> lifecycle.onRestaurantAccepted(event.orderId(), event.prepMinutes());
                case REJECTED -> lifecycle.onRestaurantRejected(event.orderId(), event.reason());
                case READY -> lifecycle.onReadyForPickup(event.orderId());
            }
        });
    }

    @KafkaListener(topics = Topics.DELIVERY_UPDATED)
    public void onDeliveryUpdate(DeliveryUpdatedEvent event) {
        safely(event.orderId(), () -> {
            switch (event.status()) {
                case ASSIGNED -> lifecycle.onDriverAssigned(event.orderId(), event.driverId(), event.driverName());
                case PICKED_UP -> lifecycle.onPickedUp(event.orderId());
                case DELIVERED -> lifecycle.onDelivered(event.orderId());
                case NO_DRIVER_AVAILABLE -> log.warn("No driver available yet for order {}", event.orderId());
            }
        });
    }

    private void safely(Long orderId, Runnable action) {
        try {
            action.run();
        } catch (InvalidStatusTransitionException ex) {
            log.warn("Skipping event for order {}: {}", orderId, ex.getMessage());
        }
    }
}
