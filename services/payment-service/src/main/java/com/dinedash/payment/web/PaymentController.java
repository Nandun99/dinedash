package com.dinedash.payment.web;

import com.dinedash.common.security.CurrentUser;
import com.dinedash.payment.dto.PaymentIntentResponse;
import com.dinedash.payment.dto.PaymentResponse;
import com.dinedash.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/orders/{orderId}")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/intent")
    public PaymentIntentResponse startPayment(@AuthenticationPrincipal CurrentUser user, @PathVariable Long orderId) {
        return paymentService.startPayment(user, orderId);
    }

    @GetMapping
    public PaymentResponse get(@AuthenticationPrincipal CurrentUser user, @PathVariable Long orderId) {
        return paymentService.forOrder(user, orderId);
    }
}
