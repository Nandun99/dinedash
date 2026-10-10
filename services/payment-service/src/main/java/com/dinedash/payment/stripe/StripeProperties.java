package com.dinedash.payment.stripe;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dinedash.stripe")
public record StripeProperties(String secretKey, String publishableKey, String webhookSecret) {
}
