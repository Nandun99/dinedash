package com.dinedash.payment.stripe;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class StripeSdkGateway implements StripeGateway {

    private final StripeClient client;

    public StripeSdkGateway(StripeProperties properties) {
        this.client = new StripeClient(properties.secretKey());
    }

    @Override
    public Intent createIntent(long amountMinor, String currency, Long orderId, Long customerId) {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountMinor)
                .setCurrency(currency.toLowerCase(Locale.ROOT))
                .setDescription("DineDash order #" + orderId)
                .putMetadata("orderId", String.valueOf(orderId))
                .putMetadata("customerId", String.valueOf(customerId))
                .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build())
                .build();
        // Same key for the same order: a double click never creates two intents
        RequestOptions options = RequestOptions.builder().setIdempotencyKey("order-" + orderId + "-intent").build();
        return call(() -> toIntent(client.paymentIntents().create(params, options)));
    }

    @Override
    public Intent retrieveIntent(String intentId) {
        return call(() -> toIntent(client.paymentIntents().retrieve(intentId)));
    }

    @Override
    public void cancelIntent(String intentId) {
        call(() -> client.paymentIntents().cancel(intentId));
    }

    @Override
    public String refund(String intentId, Long orderId) {
        RefundCreateParams params = RefundCreateParams.builder().setPaymentIntent(intentId).build();
        RequestOptions options = RequestOptions.builder().setIdempotencyKey("order-" + orderId + "-refund").build();
        return call(() -> client.refunds().create(params, options)).getId();
    }

    private static Intent toIntent(PaymentIntent pi) {
        return new Intent(pi.getId(), pi.getClientSecret(), pi.getStatus());
    }

    private static <T> T call(StripeCall<T> call) {
        try {
            return call.run();
        } catch (StripeException e) {
            throw new PaymentProviderException(e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface StripeCall<T> {
        T run() throws StripeException;
    }

}
