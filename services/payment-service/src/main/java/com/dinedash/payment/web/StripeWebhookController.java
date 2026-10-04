package com.dinedash.payment.web;

import com.dinedash.payment.service.WebhookService;
import com.dinedash.payment.stripe.StripeWebhookVerifier;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stripe is the source of truth for whether a payment succeeded; the browser is never trusted.
 * Returning a non-2xx status makes Stripe retry the event later.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeWebhookVerifier verifier;
    private final WebhookService webhookService;

    @PostMapping("/stripe")
    public ResponseEntity<Void> handle(@RequestBody String payload,
                                       @RequestHeader(name = "Stripe-Signature", required = false) String signature) {
        Event event;
        try {
            event = verifier.verify(payload, signature);
        } catch (SignatureVerificationException | IllegalArgumentException ex) {
            log.warn("Rejected webhook with invalid signature");
            return ResponseEntity.badRequest().build();
        }
        webhookService.handle(event);
        return ResponseEntity.ok().build();
    }
}
