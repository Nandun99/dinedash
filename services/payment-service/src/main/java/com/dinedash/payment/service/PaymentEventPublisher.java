package com.dinedash.payment.service;

import com.dinedash.common.event.PaymentCompletedEvent;
import com.dinedash.common.event.PaymentFailedEvent;
import com.dinedash.common.event.Topics;
import com.dinedash.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Sends synchronously: if Kafka is down the webhook fails, the transaction rolls back and
 * Stripe retries the webhook later, so the event is never lost.
 */
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void completed(Payment p) {
        send(Topics.PAYMENT_COMPLETED, p.getOrderId(), new PaymentCompletedEvent(p.getOrderId(), p.getId(),
                p.getAmount(), p.getCurrency(), p.getProviderIntentId(), Instant.now()));
    }

    public void failed(Payment p) {
        send(Topics.PAYMENT_FAILED, p.getOrderId(),
                new PaymentFailedEvent(p.getOrderId(), p.getId(), p.getFailureReason(), Instant.now()));
    }

    private void send(String topic, Long orderId, Object event) {
        try {
            kafkaTemplate.send(topic, String.valueOf(orderId), event).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing " + topic, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Could not publish " + topic + " for order " + orderId, e);
        }
    }
}
