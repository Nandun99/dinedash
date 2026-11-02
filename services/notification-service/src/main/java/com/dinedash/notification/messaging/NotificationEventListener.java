package com.dinedash.notification.messaging;

import com.dinedash.common.event.DeliveryUpdatedEvent;
import com.dinedash.common.event.OrderStatusChangedEvent;
import com.dinedash.common.event.Topics;
import com.dinedash.notification.service.NotificationService;
import com.dinedash.notification.service.OrderMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notifications;

    @KafkaListener(topics = Topics.ORDER_STATUS_CHANGED)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        OrderMessages.forStatus(event.status(), event.orderId()).ifPresent(m ->
                notifications.notify(event.customerId(), m.title(), m.body(), event.orderId(),
                        "/orders/" + event.orderId()));
    }

    @KafkaListener(topics = Topics.DELIVERY_UPDATED)
    public void onDeliveryUpdated(DeliveryUpdatedEvent event) {
        if (event.status() == DeliveryUpdatedEvent.Status.ASSIGNED && event.driverId() != null) {
            notifications.notify(event.driverId(), "New delivery",
                    "You have been assigned order #" + event.orderId() + ". Head to the restaurant.",
                    event.orderId(), "/driver");
        }
    }
}
