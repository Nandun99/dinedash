package com.dinedash.delivery.web;

import com.dinedash.common.security.CurrentUser;
import com.dinedash.delivery.dto.DeliveryResponse;
import com.dinedash.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drivers/me/deliveries")
@RequiredArgsConstructor
public class DriverDeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/current")
    public ResponseEntity<DeliveryResponse> current(@AuthenticationPrincipal CurrentUser user) {
        return deliveryService.currentForDriver(user.id())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping
    public List<DeliveryResponse> history(@AuthenticationPrincipal CurrentUser user) {
        return deliveryService.historyForDriver(user.id());
    }

    @PostMapping("/{deliveryId}/pickup")
    public DeliveryResponse pickUp(@AuthenticationPrincipal CurrentUser user, @PathVariable String deliveryId) {
        return deliveryService.pickUp(user.id(), deliveryId);
    }

    @PostMapping("/{deliveryId}/complete")
    public DeliveryResponse complete(@AuthenticationPrincipal CurrentUser user, @PathVariable String deliveryId) {
        return deliveryService.complete(user.id(), deliveryId);
    }
}
