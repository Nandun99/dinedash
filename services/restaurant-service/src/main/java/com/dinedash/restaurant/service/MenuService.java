package com.dinedash.restaurant.service;

import com.dinedash.common.error.NotFoundException;
import com.dinedash.restaurant.domain.MenuItem;
import com.dinedash.restaurant.dto.MenuItemRequest;
import com.dinedash.restaurant.dto.MenuItemResponse;
import com.dinedash.restaurant.repository.MenuItemRepository;
import com.dinedash.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;

    /** Public menu: only items currently available. */
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
        return MenuItemResponse.from(menuItemRepository.save(item));
    }

    public MenuItemResponse update(Long ownerId, String restaurantId, String itemId, MenuItemRequest request) {
        MenuItem item = getOwnedItem(ownerId, restaurantId, itemId);
        apply(item, request);
        return MenuItemResponse.from(menuItemRepository.save(item));
    }

    public MenuItemResponse setAvailable(Long ownerId, String restaurantId, String itemId, boolean available) {
        MenuItem item = getOwnedItem(ownerId, restaurantId, itemId);
        item.setAvailable(available);
        return MenuItemResponse.from(menuItemRepository.save(item));
    }

    public void delete(Long ownerId, String restaurantId, String itemId) {
        menuItemRepository.delete(getOwnedItem(ownerId, restaurantId, itemId));
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
