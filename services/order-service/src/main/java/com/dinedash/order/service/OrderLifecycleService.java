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

    Order load(Long orderId) {
        return orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new NotFoundException("Order " + orderId + " not found"));
    }
}
