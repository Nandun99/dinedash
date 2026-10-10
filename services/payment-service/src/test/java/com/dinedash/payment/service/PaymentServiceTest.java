package com.dinedash.payment.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.error.ConflictException;
import com.dinedash.common.error.NotFoundException;
import com.dinedash.common.security.CurrentUser;
import com.dinedash.common.security.Role;
import com.dinedash.payment.domain.Payment;
import com.dinedash.payment.domain.PaymentStatus;
import com.dinedash.payment.repository.PaymentRepository;
import com.dinedash.payment.stripe.StripeGateway;
import com.dinedash.payment.stripe.StripeGateway.Intent;
import com.dinedash.payment.stripe.StripeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final CurrentUser CUSTOMER = new CurrentUser(10L, "nimal@example.com", Role.CUSTOMER);

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private StripeGateway stripe;

    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(paymentRepository, stripe, new StripeProperties("sk", "pk_test", "whsec"));
    }

    @Test
    void createsIntentForTheServerSideAmount() {
        Payment payment = new Payment(1L, 10L, new BigDecimal("3150.00"), "LKR");
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
        when(stripe.createIntent(315000L, "LKR", 1L, 10L)).thenReturn(new Intent("pi_1", "secret_1", "requires_payment_method"));

        var response = service.startPayment(CUSTOMER, 1L);

        assertThat(response.clientSecret()).isEqualTo("secret_1");
        assertThat(response.publishableKey()).isEqualTo("pk_test");
        assertThat(payment.getProviderIntentId()).isEqualTo("pi_1");
    }

    @Test
    void reusesOpenIntentInsteadOfCreatingAnother() {
        Payment payment = new Payment(1L, 10L, new BigDecimal("1000.00"), "LKR");
        payment.setProviderIntentId("pi_1");
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));
        when(stripe.retrieveIntent("pi_1")).thenReturn(new Intent("pi_1", "secret_1", "requires_payment_method"));

        service.startPayment(CUSTOMER, 1L);

        verify(stripe, never()).createIntent(anyLong(), anyString(), anyLong(), anyLong());
    }

    @Test
    void askToRetryWhenOrderEventNotYetReceived() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startPayment(CUSTOMER, 1L)).isInstanceOf(ConflictException.class);
    }

    @Test
    void cannotPayForSomeoneElsesOrder() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(new Payment(1L, 99L, BigDecimal.TEN, "LKR")));

        assertThatThrownBy(() -> service.startPayment(CUSTOMER, 1L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void cannotPayTwice() {
        Payment payment = new Payment(1L, 10L, BigDecimal.TEN, "LKR");
        payment.setStatus(PaymentStatus.SUCCEEDED);
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> service.startPayment(CUSTOMER, 1L)).isInstanceOf(BadRequestException.class);
    }
}
