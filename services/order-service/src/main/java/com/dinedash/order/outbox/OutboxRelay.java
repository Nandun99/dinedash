package com.dinedash.order.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Publishes pending outbox rows in insertion order. Delivery is at-least-once, so
 * consumers must be idempotent (they are: status transitions ignore duplicates).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private static final int BATCH_SIZE = 50;

    private final OutboxRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${dinedash.outbox.poll-interval:1000}")
    @Transactional
    public void publishPending() {
        for (OutboxEvent event : repository.findByPublishedAtIsNullOrderByIdAsc(Limit.of(BATCH_SIZE))) {
            try {
                Object payload = objectMapper.readValue(event.getPayload(), Class.forName(event.getEventType()));
                kafkaTemplate.send(event.getTopic(), event.getMessageKey(), payload).get(5, TimeUnit.SECONDS);
                event.setPublishedAt(Instant.now());
            } catch (Exception ex) {
                event.setAttempts(event.getAttempts() + 1);
                log.warn("Outbox event {} to {} failed (attempt {}): {}", event.getId(), event.getTopic(),
                        event.getAttempts(), ex.getMessage());
                // Stop here to keep per-order ordering; the next run retries from this event.
                break;
            }
        }
    }
}
