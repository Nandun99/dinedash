package com.dinedash.order.service;

import com.dinedash.common.event.GeoPoint;
import com.dinedash.common.event.OrderCancelledEvent;
import com.dinedash.common.event.OrderCreatedEvent;
import com.dinedash.common.event.OrderItemLine;
import com.dinedash.common.event.OrderPaidEvent;
import com.dinedash.common.event.OrderStatusChangedEvent;
import com.dinedash.common.event.Topics;
import com.dinedash.order.domain.Order;
import com.dinedash.order.outbox.OutboxWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OrderEvents {

    private final OutboxWriter outbox;

    public void created(Order o) {
        outbox.record(Topics.ORDER_CREATED, o.getId(), new OrderCreatedEvent(o.getId(), o.getCustomerId(),
                o.getRestaurantId(), o.getTotal(), o.getCurrency(), o.getPlacedAt()));
        statusChanged(o);
    }

    public void paid(Order o) {
        outbox.record(Topics.ORDER_PAID, o.getId(), new OrderPaidEvent(o.getId(), o.getCustomerId(),
                o.getCustomerName(), o.getCustomerPhone(), o.getRestaurantId(), o.getRestaurantName(),
                o.getItems().stream()
                        .map(i -> new OrderItemLine(i.getMenuItemId(), i.getName(), i.getQuantity(), i.getUnitPrice()))
                        .toList(),
                o.getTotal(), new GeoPoint(o.getPickupLat(), o.getPickupLng()),
                new GeoPoint(o.getDropoffLat(), o.getDropoffLng()), o.getDeliveryAddress(), o.getNotes(), Instant.now()));
    }

    public void cancelled(Order o, boolean refundRequired) {
        outbox.record(Topics.ORDER_CANCELLED, o.getId(), new OrderCancelledEvent(o.getId(), o.getCustomerId(),
                o.getRestaurantId(), o.getCancelReason(), refundRequired, Instant.now()));
    }

    public void statusChanged(Order o) {
        outbox.record(Topics.ORDER_STATUS_CHANGED, o.getId(), new OrderStatusChangedEvent(o.getId(),
                o.getCustomerId(), o.getRestaurantId(), o.getStatus().name(), o.getDriverId(), Instant.now()));
    }
}
