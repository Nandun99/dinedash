package com.dinedash.payment.dto;

import java.math.BigDecimal;

public record PaymentIntentResponse(Long orderId, String clientSecret, String publishableKey, BigDecimal amount,
                                    String currency) {
}
