package com.dinedash.common.event;

/**
 * Kafka topic names shared by all services. Every message is keyed by order id,
 * so all events of one order land on the same partition and stay in order.
 */
public final class Topics {

    /** order-service: a new order is waiting for payment. */
    public static final String ORDER_CREATED = "order.created";
    /** order-service: payment confirmed, the restaurant can start. */
    public static final String ORDER_PAID = "order.paid";
    /** order-service: order cancelled or rejected; payment may need a refund. */
    public static final String ORDER_CANCELLED = "order.cancelled";
    /** order-service: any status change, used for customer notifications. */
    public static final String ORDER_STATUS_CHANGED = "order.status-changed";

    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";

    public static final String RESTAURANT_ORDER_UPDATED = "restaurant.order-updated";

    public static final String DELIVERY_UPDATED = "delivery.updated";

    private Topics() {
    }
}
