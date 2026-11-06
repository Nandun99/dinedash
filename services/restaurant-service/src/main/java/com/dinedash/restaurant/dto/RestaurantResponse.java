package com.dinedash.restaurant.dto;

import com.dinedash.restaurant.domain.Address;
import com.dinedash.restaurant.domain.Restaurant;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

public record RestaurantResponse(
        String id,
        Long ownerId,
        String name,
        String description,
        List<String> cuisines,
        String phone,
        Address address,
        double latitude,
        double longitude,
        String logoUrl,
        String coverImageUrl,
        LocalTime opensAt,
        LocalTime closesAt,
        boolean acceptingOrders,
        boolean openNow,
        BigDecimal deliveryFee,
        BigDecimal minimumOrder,
        int averagePrepMinutes,
        Restaurant.ApprovalStatus approvalStatus
) {
    public static RestaurantResponse from(Restaurant r) {
        return new RestaurantResponse(r.getId(), r.getOwnerId(), r.getName(), r.getDescription(), r.getCuisines(),
                r.getPhone(), r.getAddress(), r.getLocation().getY(), r.getLocation().getX(), r.getLogoUrl(),
                r.getCoverImageUrl(), r.getOpensAt(), r.getClosesAt(), r.isAcceptingOrders(),
                r.canTakeOrders(ZonedDateTime.now()), r.getDeliveryFee(), r.getMinimumOrder(),
                r.getAveragePrepMinutes(), r.getApprovalStatus());
    }
}
