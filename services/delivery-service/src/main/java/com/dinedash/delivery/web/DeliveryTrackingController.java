package com.dinedash.delivery.web;

import com.dinedash.common.security.CurrentUser;
import com.dinedash.delivery.dto.DeliveryResponse;
import com.dinedash.delivery.service.DeliveryService;
import com.dinedash.delivery.tracking.TrackingTokenService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryTrackingController {

    private final DeliveryService deliveryService;
    private final ObjectProvider<TrackingTokenService> tokenService;

    @GetMapping("/orders/{orderId}")
    public DeliveryResponse forOrder(@AuthenticationPrincipal CurrentUser user, @PathVariable Long orderId) {
        return deliveryService.forOrder(user, orderId);
    }

    /** 404 when Firebase is not configured; the frontend then falls back to polling. */
    @GetMapping("/orders/{orderId}/tracking-token")
    public ResponseEntity<Map<String, String>> trackingToken(@AuthenticationPrincipal CurrentUser user,
                                                             @PathVariable Long orderId) {
        TrackingTokenService tokens = tokenService.getIfAvailable();
        if (tokens == null) {
            return ResponseEntity.notFound().build();
        }
        deliveryService.forOrder(user, orderId);
        return ResponseEntity.ok(Map.of("token", tokens.tokenFor(user.id(), orderId)));
    }
}
