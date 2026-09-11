package com.dinedash.restaurant.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Authoritative prices for an order. order-service never trusts prices sent by the browser.
 */
public record QuoteResponse(
        String restaurantId,
        String restaurantName,
        double latitude,
        double longitude,
        boolean acceptingOrders,
        BigDecimal deliveryFee,
        BigDecimal minimumOrder,
        List<PricedLine> items
) {
    public record PricedLine(String menuItemId, String name, BigDecimal unitPrice, int quantity, boolean available) {
    }
}
