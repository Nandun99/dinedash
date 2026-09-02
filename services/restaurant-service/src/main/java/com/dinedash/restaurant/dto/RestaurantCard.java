package com.dinedash.restaurant.dto;

import com.dinedash.restaurant.domain.Restaurant;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

/** Lightweight view used in listings and search results. */
public record RestaurantCard(
        String id,
        String name,
        List<String> cuisines,
        String city,
        String logoUrl,
        String coverImageUrl,
        boolean openNow,
        BigDecimal deliveryFee,
        int averagePrepMinutes,
        Double distanceKm
) {
    public static RestaurantCard from(Restaurant r, Double distanceKm) {
        return new RestaurantCard(r.getId(), r.getName(), r.getCuisines(),
                r.getAddress() != null ? r.getAddress().city() : null, r.getLogoUrl(), r.getCoverImageUrl(),
                r.canTakeOrders(ZonedDateTime.now()), r.getDeliveryFee(), r.getAveragePrepMinutes(), distanceKm);
    }
}
