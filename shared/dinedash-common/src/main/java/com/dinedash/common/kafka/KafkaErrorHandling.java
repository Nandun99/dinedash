package com.dinedash.common.kafka;

import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Retries a failing record a few times with backoff, then parks it on {@code <topic>.DLT}
 * so one bad message never blocks the partition.
 */
public final class KafkaErrorHandling {

    private KafkaErrorHandling() {
    }

    public static DefaultErrorHandler retryThenDeadLetter(KafkaOperations<Object, Object> template) {
        ExponentialBackOff backOff = new ExponentialBackOff(500, 2.0);
        backOff.setMaxElapsedTime(10_000);
        DefaultErrorHandler handler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(template), backOff);
        handler.addNotRetryableExceptions(DeserializationException.class, IllegalArgumentException.class);
        return handler;
    }
}
