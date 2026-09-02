package com.dinedash.restaurant.web;

import com.dinedash.common.security.CurrentUser;
import com.dinedash.restaurant.dto.RestaurantRequest;
import com.dinedash.restaurant.dto.RestaurantResponse;
import com.dinedash.restaurant.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/owner/restaurants")
@RequiredArgsConstructor
public class OwnerRestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public List<RestaurantResponse> mine(@AuthenticationPrincipal CurrentUser user) {
        return restaurantService.listForOwner(user.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse create(@AuthenticationPrincipal CurrentUser user,
                                     @Valid @RequestBody RestaurantRequest request) {
        return restaurantService.create(user.id(), request);
    }

    @PutMapping("/{restaurantId}")
    public RestaurantResponse update(@AuthenticationPrincipal CurrentUser user, @PathVariable String restaurantId,
                                     @Valid @RequestBody RestaurantRequest request) {
        return restaurantService.update(user.id(), restaurantId, request);
    }

    @PatchMapping("/{restaurantId}/accepting-orders")
    public RestaurantResponse setAcceptingOrders(@AuthenticationPrincipal CurrentUser user,
                                                 @PathVariable String restaurantId, @RequestParam boolean value) {
        return restaurantService.setAcceptingOrders(user.id(), restaurantId, value);
    }
}
