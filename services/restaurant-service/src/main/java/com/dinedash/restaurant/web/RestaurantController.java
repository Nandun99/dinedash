package com.dinedash.restaurant.web;

import com.dinedash.restaurant.dto.MenuItemResponse;
import com.dinedash.restaurant.dto.PageResponse;
import com.dinedash.restaurant.dto.RestaurantCard;
import com.dinedash.restaurant.dto.RestaurantResponse;
import com.dinedash.restaurant.service.CatalogQueryService;
import com.dinedash.restaurant.service.MenuService;
import com.dinedash.restaurant.service.RestaurantSearchService;
import com.dinedash.restaurant.service.RestaurantSearchService.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantSearchService searchService;
    private final CatalogQueryService catalogQueryService;
    private final MenuService menuService;

    @GetMapping
    public PageResponse<RestaurantCard> search(@RequestParam(required = false) String q,
                                               @RequestParam(required = false) String cuisine,
                                               @RequestParam(required = false) Double lat,
                                               @RequestParam(required = false) Double lng,
                                               @RequestParam(required = false) Double radiusKm,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return searchService.search(new SearchCriteria(q, cuisine, lat, lng, radiusKm, page, size));
    }

    @GetMapping("/{restaurantId}")
    public RestaurantResponse get(@PathVariable String restaurantId) {
        return catalogQueryService.publicRestaurant(restaurantId).withCurrentOpenState();
    }

    @GetMapping("/{restaurantId}/menu")
    public List<MenuItemResponse> menu(@PathVariable String restaurantId) {
        return menuService.publicMenu(restaurantId);
    }
}
