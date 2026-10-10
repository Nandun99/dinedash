package com.dinedash.payment.service;

import com.dinedash.payment.domain.Payment;
import com.dinedash.payment.domain.PaymentStatus;
import com.dinedash.payment.repository.PaymentRepository;
import com.dinedash.payment.stripe.StripeGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private StripeGateway stripe;

    @InjectMocks
    private RefundService refundService;

    @Test
    void refundsSucceededPayment() {
        Payment payment = payment(PaymentStatus.SUCCEEDED);
        when(stripe.refund("pi_1", 1L)).thenReturn("re_1");

        refundService.handleCancellation(1L, true);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getProviderRefundId()).isEqualTo("re_1");
    }

    @Test
    void cancelsIntentOfUnpaidOrder() {
        Payment payment = payment(PaymentStatus.PENDING);

        refundService.handleCancellation(1L, false);

        verify(stripe).cancelIntent("pi_1");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    void secondCancellationIsANoOp() {
        payment(PaymentStatus.REFUNDED);

        refundService.handleCancellation(1L, true);

        verify(stripe, never()).refund(any(), anyLong());
    }

    private Payment payment(PaymentStatus status) {
        Payment payment = new Payment(1L, 10L, new BigDecimal("1500.00"), "LKR");
        payment.setStatus(status);
        payment.setProviderIntentId("pi_1");
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
        return payment;
    }
}
