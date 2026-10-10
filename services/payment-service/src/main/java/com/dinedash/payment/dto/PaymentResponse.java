package com.dinedash.payment.dto;

import com.dinedash.payment.domain.Payment;
import com.dinedash.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(Long id, Long orderId, BigDecimal amount, String currency, PaymentStatus status,
                              String failureReason, Instant updatedAt) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getAmount(), p.getCurrency(), p.getStatus(),
                p.getFailureReason(), p.getUpdatedAt());
    }
}
