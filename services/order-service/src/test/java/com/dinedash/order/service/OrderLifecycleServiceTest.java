package com.dinedash.order.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.error.NotFoundException;
import com.dinedash.common.security.CurrentUser;
import com.dinedash.common.security.Role;
import com.dinedash.order.domain.Order;
import com.dinedash.order.domain.OrderStatus;
import com.dinedash.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderLifecycleServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderEvents events;

    @InjectMocks
    private OrderLifecycleService lifecycle;

    @Test
    void paymentMovesOrderToPaidAndNotifiesRestaurant() {
        Order order = order(OrderStatus.PENDING_PAYMENT);

        lifecycle.onPaymentCompleted(1L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        verify(events).paid(order);
    }

    @Test
    void duplicatePaymentEventPublishesNothing() {
        Order order = order(OrderStatus.PAID);

        lifecycle.onPaymentCompleted(1L);

        verify(events, never()).paid(order);
    }

    @Test
    void paymentForCancelledOrderRequestsRefund() {
        Order order = order(OrderStatus.CANCELLED);

        lifecycle.onPaymentCompleted(1L);

        verify(events).cancelled(order, true);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancellingPaidOrderRequestsRefund() {
        Order order = order(OrderStatus.PAID);

        lifecycle.cancelByCustomer(new CurrentUser(10L, null, Role.CUSTOMER), 1L, null);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(events).cancelled(order, true);
    }

    @Test
    void cannotCancelAfterRestaurantAccepted() {
        order(OrderStatus.ACCEPTED);

        assertThatThrownBy(() -> lifecycle.cancelByCustomer(new CurrentUser(10L, null, Role.CUSTOMER), 1L, null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void cannotCancelSomeoneElsesOrder() {
        order(OrderStatus.PAID);

        assertThatThrownBy(() -> lifecycle.cancelByCustomer(new CurrentUser(99L, null, Role.CUSTOMER), 1L, null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void rejectionTriggersRefund() {
        Order order = order(OrderStatus.PAID);

        lifecycle.onRestaurantRejected(1L, "Out of ingredients");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(order.getCancelReason()).isEqualTo("Out of ingredients");
        verify(events, times(1)).cancelled(order, true);
    }

    private Order order(OrderStatus status) {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(10L);
        // walk the state machine to reach the requested status
        switch (status) {
            case PAID -> order.transitionTo(OrderStatus.PAID, null);
            case ACCEPTED -> {
                order.transitionTo(OrderStatus.PAID, null);
                order.transitionTo(OrderStatus.ACCEPTED, null);
            }
            case CANCELLED -> order.transitionTo(OrderStatus.CANCELLED, null);
            default -> {
            }
        }
        when(orderRepository.findWithItemsById(1L)).thenReturn(Optional.of(order));
        return order;
    }
}
