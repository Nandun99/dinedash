package com.dinedash.restaurant.web;

import com.dinedash.restaurant.dto.QuoteRequest;
import com.dinedash.restaurant.dto.QuoteResponse;
import com.dinedash.restaurant.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service API. Not routed by the gateway; only reachable inside the cluster.
 */
@RestController
@RequestMapping("/internal/restaurants")
@RequiredArgsConstructor
public class InternalRestaurantController {

    private final QuoteService quoteService;

    @PostMapping("/{restaurantId}/quote")
    public QuoteResponse quote(@PathVariable String restaurantId, @Valid @RequestBody QuoteRequest request) {
        return quoteService.quote(restaurantId, request);
    }
}
