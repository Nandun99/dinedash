package com.dinedash.payment.stripe;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StripeWebhookVerifier {

    private final StripeProperties properties;

    /**
     * Checks the Stripe-Signature header against the raw body. Anything that fails is not from Stripe.
     */
    public Event verify(String payload, String signatureHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, signatureHeader, properties.webhookSecret());
    }
}
