package com.dinedash.order.domain;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAYMENT_FAILED,
    PAID,
    ACCEPTED,
    READY_FOR_PICKUP,
    PICKED_UP,
    DELIVERED,
    REJECTED,
    CANCELLED;

    static {
        PENDING_PAYMENT.next = EnumSet.of(PAID, PAYMENT_FAILED, CANCELLED);
        PAYMENT_FAILED.next = EnumSet.of(PAID, CANCELLED);
        PAID.next = EnumSet.of(ACCEPTED, REJECTED, CANCELLED);
        ACCEPTED.next = EnumSet.of(READY_FOR_PICKUP, CANCELLED);
        READY_FOR_PICKUP.next = EnumSet.of(PICKED_UP);
        PICKED_UP.next = EnumSet.of(DELIVERED);
        DELIVERED.next = EnumSet.noneOf(OrderStatus.class);
        REJECTED.next = EnumSet.noneOf(OrderStatus.class);
        CANCELLED.next = EnumSet.noneOf(OrderStatus.class);
    }

    private Set<OrderStatus> next;

    public boolean canMoveTo(OrderStatus target) {
        return next.contains(target);
    }

    public boolean isTerminal() {
        return next.isEmpty();
    }

    /** Customers may cancel until the restaurant has accepted the order. */
    public boolean isCancellableByCustomer() {
        return this == PENDING_PAYMENT || this == PAYMENT_FAILED || this == PAID;
    }
}
