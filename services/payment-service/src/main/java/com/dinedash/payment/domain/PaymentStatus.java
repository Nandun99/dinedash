package com.dinedash.payment.domain;

public enum PaymentStatus {
    /** Waiting for the customer to pay. */
    PENDING,
    SUCCEEDED,
    FAILED,
    /** Order cancelled before the customer paid. */
    CANCELLED,
    REFUNDED
}
