package com.dinedash.payment.repository;

import com.dinedash.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByProviderIntentId(String providerIntentId);

    boolean existsByOrderId(Long orderId);
}
