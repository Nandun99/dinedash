package com.dinedash.order.web;

import com.dinedash.common.security.CurrentUser;
import com.dinedash.order.dto.OrderResponse;
import com.dinedash.order.dto.OrderSummary;
import com.dinedash.order.dto.PlaceOrderRequest;
import com.dinedash.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@AuthenticationPrincipal CurrentUser user, @Valid @RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(user, request);
    }

    @GetMapping
    public Page<OrderSummary> mine(@AuthenticationPrincipal CurrentUser user, @PageableDefault(size = 10) Pageable pageable) {
        return orderService.myOrders(user.id(), pageable);
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@AuthenticationPrincipal CurrentUser user, @PathVariable Long orderId) {
        return orderService.get(user, orderId);
    }
}
