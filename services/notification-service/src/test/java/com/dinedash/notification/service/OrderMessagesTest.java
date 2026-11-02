package com.dinedash.notification.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMessagesTest {

    @Test
    void hasFriendlyMessageForCustomerFacingStatuses() {
        assertThat(OrderMessages.forStatus("PICKED_UP", 42L))
                .hasValueSatisfying(m -> assertThat(m.body()).contains("#42"));
        assertThat(OrderMessages.forStatus("DELIVERED", 42L)).isPresent();
    }

    @Test
    void skipsInternalStatuses() {
        assertThat(OrderMessages.forStatus("PENDING_PAYMENT", 42L)).isEmpty();
    }
}
