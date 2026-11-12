package com.dinedash.restaurant.web;

import com.dinedash.common.error.NotFoundException;
import com.dinedash.restaurant.cache.CatalogCacheEvictor;
import com.dinedash.restaurant.domain.Restaurant;
import com.dinedash.restaurant.dto.RestaurantResponse;
import com.dinedash.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/restaurants")
@RequiredArgsConstructor
public class AdminRestaurantController {

    private final RestaurantRepository restaurantRepository;
    private final CatalogCacheEvictor cacheEvictor;

    @GetMapping
    public Page<RestaurantResponse> list(@RequestParam(required = false) Restaurant.ApprovalStatus status,
                                         @PageableDefault(size = 20) Pageable pageable) {
        Page<Restaurant> page = status == null
                ? restaurantRepository.findAll(pageable)
                : restaurantRepository.findByApprovalStatus(status, pageable);
        return page.map(RestaurantResponse::from);
    }

    @PatchMapping("/{restaurantId}/approval")
    public RestaurantResponse setApproval(@PathVariable String restaurantId,
                                          @RequestParam Restaurant.ApprovalStatus value) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
        restaurant.setApprovalStatus(value);
        log.info("Restaurant {} is now {}", restaurantId, value);
        RestaurantResponse saved = RestaurantResponse.from(restaurantRepository.save(restaurant));
        cacheEvictor.restaurantChanged(restaurantId);
        return saved;
    }
}
