package com.dinedash.payment.stripe;

/**
 * Thin seam over the Stripe SDK so the payment logic can be unit tested without network calls.
 */
public interface StripeGateway {

    Intent createIntent(long amountMinor, String currency, Long orderId, Long customerId);

    Intent retrieveIntent(String intentId);

    void cancelIntent(String intentId);

    String refund(String intentId, Long orderId);

    record Intent(String id, String clientSecret, String status) {

        public boolean isReusable() {
            return status.startsWith("requires_");
        }
    }
}
