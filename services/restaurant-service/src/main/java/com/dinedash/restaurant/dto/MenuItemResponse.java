package com.dinedash.restaurant.dto;

import com.dinedash.restaurant.domain.MenuItem;

import java.math.BigDecimal;

public record MenuItemResponse(
        String id,
        String restaurantId,
        String name,
        String description,
        String category,
        BigDecimal price,
        String imageUrl,
        boolean available,
        boolean vegetarian,
        boolean spicy
) {
    public static MenuItemResponse from(MenuItem item) {
        return new MenuItemResponse(item.getId(), item.getRestaurantId(), item.getName(), item.getDescription(),
                item.getCategory(), item.getPrice(), item.getImageUrl(), item.isAvailable(), item.isVegetarian(),
                item.isSpicy());
    }
}
