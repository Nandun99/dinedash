package com.dinedash.payment.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.error.ConflictException;
import com.dinedash.common.error.NotFoundException;
import com.dinedash.common.security.CurrentUser;
import com.dinedash.payment.domain.Payment;
import com.dinedash.payment.domain.PaymentStatus;
import com.dinedash.payment.dto.PaymentIntentResponse;
import com.dinedash.payment.dto.PaymentResponse;
import com.dinedash.payment.repository.PaymentRepository;
import com.dinedash.payment.stripe.StripeGateway;
import com.dinedash.payment.stripe.StripeGateway.Intent;
import com.dinedash.payment.stripe.StripeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripeGateway stripe;
    private final StripeProperties stripeProperties;

    @Transactional
    public PaymentIntentResponse startPayment(CurrentUser customer, Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                // order.created may still be in flight right after checkout
                .orElseThrow(() -> new ConflictException("Payment is being prepared, please retry in a moment"));
        if (!Objects.equals(payment.getCustomerId(), customer.id())) {
            throw new NotFoundException("Order not found");
        }
        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.FAILED) {
            throw new BadRequestException("This order is already " + payment.getStatus().name().toLowerCase());
        }

        Intent intent = null;
        if (payment.getProviderIntentId() != null) {
            Intent existing = stripe.retrieveIntent(payment.getProviderIntentId());
            if (existing.isReusable()) {
                intent = existing;
            }
        }
        if (payment.getStatus() == PaymentStatus.FAILED) {
            // Customer is retrying with another card
            payment.setStatus(PaymentStatus.PENDING);
        }
        if (intent == null) {
            intent = stripe.createIntent(payment.amountInMinorUnits(), payment.getCurrency(), orderId, customer.id());
            payment.setProviderIntentId(intent.id());
            log.info("Created payment intent {} for order {}", intent.id(), orderId);
        }
        return new PaymentIntentResponse(orderId, intent.clientSecret(), stripeProperties.publishableKey(),
                payment.getAmount(), payment.getCurrency());
    }

    @Transactional(readOnly = true)
    public PaymentResponse forOrder(CurrentUser user, Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .filter(p -> Objects.equals(p.getCustomerId(), user.id()))
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        return PaymentResponse.from(payment);
    }
}
