package com.dinedash.order.dto;

import com.dinedash.order.domain.Order;
import com.dinedash.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummary(Long id, String restaurantName, OrderStatus status, BigDecimal total, int itemCount,
                           Instant placedAt) {

    public static OrderSummary from(Order o) {
        int count = o.getItems().stream().mapToInt(i -> i.getQuantity()).sum();
        return new OrderSummary(o.getId(), o.getRestaurantName(), o.getStatus(), o.getTotal(), count, o.getPlacedAt());
    }
}
