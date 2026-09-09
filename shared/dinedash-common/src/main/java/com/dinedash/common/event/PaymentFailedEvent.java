package com.dinedash.common.event;

import java.time.Instant;

public record PaymentFailedEvent(Long orderId, Long paymentId, String reason, Instant failedAt) {
}
