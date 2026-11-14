package com.dinedash.restaurant.service;

import com.dinedash.common.error.NotFoundException;
import com.dinedash.restaurant.cache.CacheNames;
import com.dinedash.restaurant.cache.CatalogCacheEvictor;
import com.dinedash.restaurant.domain.MenuItem;
import com.dinedash.restaurant.dto.MenuItemRequest;
import com.dinedash.restaurant.dto.MenuItemResponse;
import com.dinedash.restaurant.repository.MenuItemRepository;
import com.dinedash.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;
    private final CatalogCacheEvictor cacheEvictor;

    /** Public menu: only items currently available. */
    @Cacheable(cacheNames = CacheNames.MENUS, key = "#restaurantId")
    public List<MenuItemResponse> publicMenu(String restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NotFoundException("Restaurant not found");
        }
        return menuItemRepository.findByRestaurantIdAndAvailableTrueOrderByCategoryAscNameAsc(restaurantId).stream()
                .map(MenuItemResponse::from)
                .toList();
    }

    /** Owner menu: everything, including items marked unavailable. */
    public List<MenuItemResponse> ownerMenu(Long ownerId, String restaurantId) {
        restaurantService.getOwned(ownerId, restaurantId);
        return menuItemRepository.findByRestaurantIdOrderByCategoryAscNameAsc(restaurantId).stream()
                .map(MenuItemResponse::from)
                .toList();
    }

    public MenuItemResponse create(Long ownerId, String restaurantId, MenuItemRequest request) {
        restaurantService.getOwned(ownerId, restaurantId);
        MenuItem item = new MenuItem();
        item.setRestaurantId(restaurantId);
        apply(item, request);
        MenuItemResponse saved = MenuItemResponse.from(menuItemRepository.save(item));
        cacheEvictor.menuChanged(restaurantId);
        return saved;
    }

    public MenuItemResponse update(Long ownerId, String restaurantId, String itemId, MenuItemRequest request) {
        MenuItem item = getOwnedItem(ownerId, restaurantId, itemId);
        apply(item, request);
        MenuItemResponse saved = MenuItemResponse.from(menuItemRepository.save(item));
        cacheEvictor.menuChanged(restaurantId);
        return saved;
    }

    public MenuItemResponse setAvailable(Long ownerId, String restaurantId, String itemId, boolean available) {
        MenuItem item = getOwnedItem(ownerId, restaurantId, itemId);
        item.setAvailable(available);
        MenuItemResponse saved = MenuItemResponse.from(menuItemRepository.save(item));
        cacheEvictor.menuChanged(restaurantId);
        return saved;
    }

    public void delete(Long ownerId, String restaurantId, String itemId) {
        menuItemRepository.delete(getOwnedItem(ownerId, restaurantId, itemId));
        cacheEvictor.menuChanged(restaurantId);
    }

    MenuItem getOwnedItem(Long ownerId, String restaurantId, String itemId) {
        restaurantService.getOwned(ownerId, restaurantId);
        return menuItemRepository.findByIdAndRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> new NotFoundException("Menu item not found"));
    }

    private static void apply(MenuItem item, MenuItemRequest request) {
        item.setName(request.name().trim());
        item.setDescription(request.description());
        item.setCategory(request.category().trim());
        item.setPrice(request.price());
        item.setVegetarian(request.vegetarian());
        item.setSpicy(request.spicy());
    }
}
