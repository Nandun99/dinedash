package com.dinedash.order.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.error.NotFoundException;
import com.dinedash.common.security.CurrentUser;
import com.dinedash.order.domain.Order;
import com.dinedash.order.domain.OrderStatus;
import com.dinedash.order.dto.OrderResponse;
import com.dinedash.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * All status changes go through here so the rules live in one place.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderLifecycleService {

    private final OrderRepository orderRepository;
    private final OrderEvents events;

    @Transactional
    public OrderResponse cancelByCustomer(CurrentUser customer, Long orderId, String reason) {
        Order order = load(orderId);
        if (!Objects.equals(order.getCustomerId(), customer.id())) {
            throw new NotFoundException("Order not found");
        }
        if (!order.getStatus().isCancellableByCustomer()) {
            throw new BadRequestException("The restaurant has already started on this order");
        }
        String why = reason == null || reason.isBlank() ? "Cancelled by customer" : reason.trim();
        boolean wasPaid = order.getStatus() == OrderStatus.PAID;
        order.setCancelReason(why);
        order.transitionTo(OrderStatus.CANCELLED, why);
        events.cancelled(order, wasPaid);
        events.statusChanged(order);
        log.info("Order {} cancelled by customer", orderId);
        return OrderResponse.from(order);
    }

    @Transactional
    public void onPaymentCompleted(Long orderId) {
        Order order = load(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            // Paid after the customer cancelled: ask payment-service to refund
            log.warn("Payment completed for cancelled order {}, requesting refund", orderId);
            events.cancelled(order, true);
            return;
        }
        if (order.transitionTo(OrderStatus.PAID, "Payment received")) {
            events.paid(order);
            events.statusChanged(order);
        }
    }

    @Transactional
    public void onPaymentFailed(Long orderId, String reason) {
        Order order = load(orderId);
        if (order.getStatus().canMoveTo(OrderStatus.PAYMENT_FAILED)
                && order.transitionTo(OrderStatus.PAYMENT_FAILED, reason)) {
            events.statusChanged(order);
        }
    }

    @Transactional
    public void onRestaurantAccepted(Long orderId, Integer prepMinutes) {
        Order order = load(orderId);
        String note = prepMinutes != null ? "Ready in about " + prepMinutes + " min" : null;
        if (order.transitionTo(OrderStatus.ACCEPTED, note)) {
            events.statusChanged(order);
        }
    }

    @Transactional
    public void onRestaurantRejected(Long orderId, String reason) {
        Order order = load(orderId);
        String why = reason != null ? reason : "Rejected by restaurant";
        order.setCancelReason(why);
        if (order.transitionTo(OrderStatus.REJECTED, why)) {
            events.cancelled(order, true);
            events.statusChanged(order);
        }
    }

    @Transactional
    public void onReadyForPickup(Long orderId) {
        Order order = load(orderId);
        if (order.transitionTo(OrderStatus.READY_FOR_PICKUP, null)) {
            events.statusChanged(order);
        }
    }

    @Transactional
    public void onDriverAssigned(Long orderId, Long driverId, String driverName) {
        Order order = load(orderId);
        if (order.getStatus().isTerminal()) {
            return;
        }
        order.setDriverId(driverId);
        order.setDriverName(driverName);
        events.statusChanged(order);
    }

    @Transactional
    public void onPickedUp(Long orderId) {
        Order order = load(orderId);
        if (order.transitionTo(OrderStatus.PICKED_UP, "On the way")) {
            events.statusChanged(order);
        }
    }

    @Transactional
    public void onDelivered(Long orderId) {
        Order order = load(orderId);
        if (order.transitionTo(OrderStatus.DELIVERED, null)) {
            events.statusChanged(order);
        }
    }

    Order load(Long orderId) {
        return orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new NotFoundException("Order " + orderId + " not found"));
    }
}
