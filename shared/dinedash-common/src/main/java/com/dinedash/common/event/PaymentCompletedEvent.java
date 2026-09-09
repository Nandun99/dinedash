package com.dinedash.common.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompletedEvent(
        Long orderId,
        Long paymentId,
        BigDecimal amount,
        String currency,
        String providerReference,
        Instant paidAt
) {
}
