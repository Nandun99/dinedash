package com.dinedash.restaurant.web;

import com.dinedash.common.security.CurrentUser;
import com.dinedash.restaurant.domain.RestaurantOrder;
import com.dinedash.restaurant.service.KitchenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/owner/restaurants/{restaurantId}/orders")
@RequiredArgsConstructor
public class OwnerOrderController {

    private final KitchenService kitchenService;

    @GetMapping
    public List<RestaurantOrder> list(@AuthenticationPrincipal CurrentUser user, @PathVariable String restaurantId,
                                      @RequestParam(defaultValue = "true") boolean active) {
        return active
                ? kitchenService.activeOrders(user.id(), restaurantId)
                : kitchenService.recentOrders(user.id(), restaurantId);
    }

    @PostMapping("/{orderId}/accept")
    public RestaurantOrder accept(@AuthenticationPrincipal CurrentUser user, @PathVariable String restaurantId,
                                  @PathVariable Long orderId, @Valid @RequestBody AcceptRequest request) {
        return kitchenService.accept(user.id(), restaurantId, orderId, request.prepMinutes());
    }

    @PostMapping("/{orderId}/reject")
    public RestaurantOrder reject(@AuthenticationPrincipal CurrentUser user, @PathVariable String restaurantId,
                                  @PathVariable Long orderId, @Valid @RequestBody RejectRequest request) {
        return kitchenService.reject(user.id(), restaurantId, orderId, request.reason());
    }

    @PostMapping("/{orderId}/ready")
    public RestaurantOrder ready(@AuthenticationPrincipal CurrentUser user, @PathVariable String restaurantId,
                                 @PathVariable Long orderId) {
        return kitchenService.markReady(user.id(), restaurantId, orderId);
    }

    public record AcceptRequest(@Min(5) @Max(120) int prepMinutes) {
    }

    public record RejectRequest(@NotBlank @Size(max = 300) String reason) {
    }
}
