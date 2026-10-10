package com.dinedash.payment.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    @Test
    void convertsAmountToMinorUnits() {
        assertThat(new Payment(1L, 2L, new BigDecimal("3150.50"), "LKR").amountInMinorUnits()).isEqualTo(315050L);
        assertThat(new Payment(1L, 2L, new BigDecimal("990"), "LKR").amountInMinorUnits()).isEqualTo(99000L);
    }
}
