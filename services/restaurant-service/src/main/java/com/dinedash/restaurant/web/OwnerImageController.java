package com.dinedash.restaurant.web;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.error.NotFoundException;
import com.dinedash.common.security.CurrentUser;
import com.dinedash.restaurant.cache.CatalogCacheEvictor;
import com.dinedash.restaurant.domain.MenuItem;
import com.dinedash.restaurant.domain.Restaurant;
import com.dinedash.restaurant.dto.ImageRequests.AttachImageRequest;
import com.dinedash.restaurant.dto.ImageRequests.UploadUrlRequest;
import com.dinedash.restaurant.repository.MenuItemRepository;
import com.dinedash.restaurant.repository.RestaurantRepository;
import com.dinedash.restaurant.service.RestaurantService;
import com.dinedash.restaurant.storage.ImageUploadService;
import com.dinedash.restaurant.storage.ImageUploadService.UploadTicket;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/owner/restaurants/{restaurantId}/images")
@RequiredArgsConstructor
public class OwnerImageController {

    private final RestaurantService restaurantService;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final ImageUploadService imageUploadService;
    private final CatalogCacheEvictor cacheEvictor;

    @PostMapping("/upload-url")
    public UploadTicket uploadUrl(@AuthenticationPrincipal CurrentUser user, @PathVariable String restaurantId,
                                  @Valid @RequestBody UploadUrlRequest request) {
        restaurantService.getOwned(user.id(), restaurantId);
        return imageUploadService.createUploadUrl(restaurantId, request.target(), request.contentType());
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> attach(@AuthenticationPrincipal CurrentUser user, @PathVariable String restaurantId,
                                      @Valid @RequestBody AttachImageRequest request) {
        Restaurant restaurant = restaurantService.getOwned(user.id(), restaurantId);
        String url = imageUploadService.publicUrlFor(restaurantId, request.key());
        switch (request.target()) {
            case LOGO -> {
                restaurant.setLogoUrl(url);
                restaurantRepository.save(restaurant);
                cacheEvictor.restaurantChanged(restaurantId);
            }
            case COVER -> {
                restaurant.setCoverImageUrl(url);
                restaurantRepository.save(restaurant);
                cacheEvictor.restaurantChanged(restaurantId);
            }
            case MENU_ITEM -> {
                if (request.menuItemId() == null) {
                    throw new BadRequestException("menuItemId is required for menu item images");
                }
                MenuItem item = menuItemRepository.findByIdAndRestaurantId(request.menuItemId(), restaurantId)
                        .orElseThrow(() -> new NotFoundException("Menu item not found"));
                item.setImageUrl(url);
                menuItemRepository.save(item);
                cacheEvictor.menuChanged(restaurantId);
            }
        }
        return Map.of("url", url);
    }
}
