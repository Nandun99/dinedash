package com.dinedash.restaurant.service;

import com.dinedash.common.error.NotFoundException;
import com.dinedash.restaurant.cache.CacheNames;
import com.dinedash.restaurant.dto.RestaurantResponse;
import com.dinedash.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogQueryService {

    private final RestaurantRepository restaurantRepository;

    /**
     * Cached public restaurant view. "openNow" depends on the clock, so callers
     * must refresh it with {@link RestaurantResponse#withCurrentOpenState()}.
     */
    @Cacheable(cacheNames = CacheNames.RESTAURANTS, key = "#restaurantId")
    public RestaurantResponse publicRestaurant(String restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .filter(r -> r.isVisible())
                .map(RestaurantResponse::from)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
    }
}
