package com.dinedash.delivery.service;

import com.dinedash.common.event.DeliveryUpdatedEvent;
import com.dinedash.common.event.Topics;
import com.dinedash.delivery.domain.Delivery;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DeliveryEvents {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(Delivery d, DeliveryUpdatedEvent.Status status) {
        kafkaTemplate.send(Topics.DELIVERY_UPDATED, String.valueOf(d.getOrderId()),
                new DeliveryUpdatedEvent(d.getOrderId(), d.getId(), d.getDriverId(), d.getDriverName(), status,
                        Instant.now()));
    }
}
