package com.dinedash.order.domain;

import com.dinedash.common.error.ConflictException;

public class InvalidStatusTransitionException extends ConflictException {

    public InvalidStatusTransitionException(OrderStatus from, OrderStatus to) {
        super("Order cannot move from " + from + " to " + to);
    }
}
