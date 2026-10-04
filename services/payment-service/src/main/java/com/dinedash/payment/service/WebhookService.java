package com.dinedash.payment.service;

import com.dinedash.payment.domain.ProcessedWebhookEvent;
import com.dinedash.payment.repository.ProcessedWebhookEventRepository;
import com.stripe.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final ProcessedWebhookEventRepository processedEvents;

    @Transactional
    public void handle(Event event) {
        if (processedEvents.existsById(event.getId())) {
            log.debug("Webhook {} already processed", event.getId());
            return;
        }
        log.info("Received Stripe event {} ({})", event.getType(), event.getId());
        processedEvents.save(new ProcessedWebhookEvent(event.getId(), event.getType()));
    }
}
