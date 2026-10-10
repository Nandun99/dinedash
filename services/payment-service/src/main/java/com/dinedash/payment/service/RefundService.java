package com.dinedash.payment.service;

import com.dinedash.payment.domain.Payment;
import com.dinedash.payment.domain.PaymentStatus;
import com.dinedash.payment.repository.PaymentRepository;
import com.dinedash.payment.stripe.StripeGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

    private final PaymentRepository paymentRepository;
    private final StripeGateway stripe;

    /**
     * Called when an order is cancelled or rejected. Paid orders are refunded in full;
     * unpaid ones have their payment intent cancelled so the customer cannot pay late.
     */
    @Transactional
    public void handleCancellation(Long orderId, boolean refundRequested) {
        paymentRepository.findByOrderId(orderId).ifPresentOrElse(
                payment -> settle(payment, refundRequested),
                () -> log.warn("Cancellation for order {} but no payment record", orderId));
    }

    private void settle(Payment payment, boolean refundRequested) {
        switch (payment.getStatus()) {
            case SUCCEEDED -> {
                if (!refundRequested) {
                    log.warn("Order {} cancelled without refund flag although it was paid; refunding anyway",
                            payment.getOrderId());
                }
                String refundId = stripe.refund(payment.getProviderIntentId(), payment.getOrderId());
                payment.setProviderRefundId(refundId);
                payment.setStatus(PaymentStatus.REFUNDED);
                log.info("Refunded order {} ({})", payment.getOrderId(), refundId);
            }
            case PENDING, FAILED -> {
                if (payment.getProviderIntentId() != null) {
                    stripe.cancelIntent(payment.getProviderIntentId());
                }
                payment.setStatus(PaymentStatus.CANCELLED);
                log.info("Cancelled unpaid payment for order {}", payment.getOrderId());
            }
            case CANCELLED, REFUNDED -> log.debug("Order {} already settled", payment.getOrderId());
        }
    }
}
