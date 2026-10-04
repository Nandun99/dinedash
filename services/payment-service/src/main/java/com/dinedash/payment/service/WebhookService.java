package com.dinedash.payment.service;

import com.dinedash.payment.domain.Payment;
import com.dinedash.payment.domain.PaymentStatus;
import com.dinedash.payment.domain.ProcessedWebhookEvent;
import com.dinedash.payment.repository.PaymentRepository;
import com.dinedash.payment.repository.ProcessedWebhookEventRepository;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    static final String SUCCEEDED = "payment_intent.succeeded";
    static final String FAILED = "payment_intent.payment_failed";

    private final ProcessedWebhookEventRepository processedEvents;
    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher publisher;

    @Transactional
    public void handle(Event event) {
        if (processedEvents.existsById(event.getId())) {
            log.debug("Webhook {} already processed", event.getId());
            return;
        }
        switch (event.getType()) {
            case SUCCEEDED -> intent(event).ifPresent(this::onSucceeded);
            case FAILED -> intent(event).ifPresent(this::onFailed);
            default -> log.debug("Ignoring Stripe event type {}", event.getType());
        }
        processedEvents.save(new ProcessedWebhookEvent(event.getId(), event.getType()));
    }

    void onSucceeded(PaymentIntent intent) {
        findPayment(intent).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
                return;
            }
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setFailureReason(null);
            log.info("Payment for order {} succeeded", payment.getOrderId());
            publisher.completed(payment);
        });
    }

    void onFailed(PaymentIntent intent) {
        findPayment(intent).ifPresent(payment -> {
            if (payment.getStatus() != PaymentStatus.PENDING) {
                return;
            }
            String reason = intent.getLastPaymentError() != null
                    ? intent.getLastPaymentError().getMessage()
                    : "Payment was declined";
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(reason);
            log.info("Payment for order {} failed: {}", payment.getOrderId(), reason);
            publisher.failed(payment);
        });
    }

    private Optional<Payment> findPayment(PaymentIntent intent) {
        Optional<Payment> payment = paymentRepository.findByProviderIntentId(intent.getId());
        if (payment.isEmpty()) {
            log.warn("No payment found for intent {}", intent.getId());
        }
        return payment;
    }

    private static Optional<PaymentIntent> intent(Event event) {
        var deserializer = event.getDataObjectDeserializer();
        StripeObject object = deserializer.getObject().orElse(null);
        if (object == null) {
            // Event was sent with a different API version than the SDK; still readable for our fields
            try {
                object = deserializer.deserializeUnsafe();
            } catch (EventDataObjectDeserializationException e) {
                log.error("Could not read Stripe event {}", event.getId(), e);
                return Optional.empty();
            }
        }
        return object instanceof PaymentIntent pi ? Optional.of(pi) : Optional.empty();
    }
}
