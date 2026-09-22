package com.dinedash.order.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OutboxWriter {

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;

    /** Must run inside the caller's transaction so the event commits (or rolls back) with the order. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String topic, Object key, Object event) {
        try {
            repository.save(new OutboxEvent(topic, String.valueOf(key), event.getClass().getName(),
                    objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise " + event.getClass().getSimpleName(), e);
        }
    }
}
