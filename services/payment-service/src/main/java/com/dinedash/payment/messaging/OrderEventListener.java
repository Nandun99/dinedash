package com.dinedash.payment.messaging;

import com.dinedash.common.event.OrderCreatedEvent;
import com.dinedash.common.event.Topics;
import com.dinedash.payment.domain.Payment;
import com.dinedash.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The amount to charge comes from order-service through Kafka, never from the browser.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final PaymentRepository paymentRepository;

    @KafkaListener(topics = Topics.ORDER_CREATED)
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        if (paymentRepository.existsByOrderId(event.orderId())) {
            return;
        }
        paymentRepository.save(new Payment(event.orderId(), event.customerId(), event.totalAmount(), event.currency()));
        log.info("Payment record created for order {} ({} {})", event.orderId(), event.totalAmount(), event.currency());
    }
}
