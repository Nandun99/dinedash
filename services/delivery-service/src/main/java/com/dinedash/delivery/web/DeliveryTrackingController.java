package com.dinedash.delivery.web;

import com.dinedash.common.security.CurrentUser;
import com.dinedash.delivery.dto.DeliveryResponse;
import com.dinedash.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryTrackingController {

    private final DeliveryService deliveryService;

    @GetMapping("/orders/{orderId}")
    public DeliveryResponse forOrder(@AuthenticationPrincipal CurrentUser user, @PathVariable Long orderId) {
        return deliveryService.forOrder(user, orderId);
    }
}
