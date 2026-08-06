package com.dinedash.common.event;

/**
 * Kafka topic names shared by all services.
 */
public final class Topics {

    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_CANCELLED = "order.cancelled";

    private Topics() {
    }
}
