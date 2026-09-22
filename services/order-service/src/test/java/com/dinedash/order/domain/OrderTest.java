package com.dinedash.order.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void followsHappyPath() {
        Order order = new Order();

        assertThat(order.transitionTo(OrderStatus.PAID, null)).isTrue();
        assertThat(order.transitionTo(OrderStatus.ACCEPTED, null)).isTrue();
        assertThat(order.transitionTo(OrderStatus.READY_FOR_PICKUP, null)).isTrue();
        assertThat(order.transitionTo(OrderStatus.PICKED_UP, null)).isTrue();
        assertThat(order.transitionTo(OrderStatus.DELIVERED, null)).isTrue();

        assertThat(order.getStatus().isTerminal()).isTrue();
        assertThat(order.getHistory()).extracting(OrderStatusChange::getStatus)
                .containsExactly(OrderStatus.PAID, OrderStatus.ACCEPTED, OrderStatus.READY_FOR_PICKUP,
                        OrderStatus.PICKED_UP, OrderStatus.DELIVERED);
    }

    @Test
    void duplicateEventIsIgnored() {
        Order order = new Order();
        order.transitionTo(OrderStatus.PAID, null);

        assertThat(order.transitionTo(OrderStatus.PAID, null)).isFalse();
        assertThat(order.getHistory()).hasSize(1);
    }

    @Test
    void cannotSkipSteps() {
        Order order = new Order();

        assertThatThrownBy(() -> order.transitionTo(OrderStatus.DELIVERED, null))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void customerCanOnlyCancelBeforeAcceptance() {
        assertThat(OrderStatus.PAID.isCancellableByCustomer()).isTrue();
        assertThat(OrderStatus.ACCEPTED.isCancellableByCustomer()).isFalse();
        assertThat(OrderStatus.PICKED_UP.isCancellableByCustomer()).isFalse();
    }
}
