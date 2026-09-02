package com.dinedash.restaurant.web;

import com.dinedash.common.security.CurrentUser;
import com.dinedash.restaurant.dto.MenuItemRequest;
import com.dinedash.restaurant.dto.MenuItemResponse;
import com.dinedash.restaurant.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/owner/restaurants/{restaurantId}/menu-items")
@RequiredArgsConstructor
public class OwnerMenuController {

    private final MenuService menuService;

    @GetMapping
    public List<MenuItemResponse> list(@AuthenticationPrincipal CurrentUser user, @PathVariable String restaurantId) {
        return menuService.ownerMenu(user.id(), restaurantId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse create(@AuthenticationPrincipal CurrentUser user, @PathVariable String restaurantId,
                                   @Valid @RequestBody MenuItemRequest request) {
        return menuService.create(user.id(), restaurantId, request);
    }

    @PutMapping("/{itemId}")
    public MenuItemResponse update(@AuthenticationPrincipal CurrentUser user, @PathVariable String restaurantId,
                                   @PathVariable String itemId, @Valid @RequestBody MenuItemRequest request) {
        return menuService.update(user.id(), restaurantId, itemId, request);
    }

    @PatchMapping("/{itemId}/available")
    public MenuItemResponse setAvailable(@AuthenticationPrincipal CurrentUser user, @PathVariable String restaurantId,
                                         @PathVariable String itemId, @RequestParam boolean value) {
        return menuService.setAvailable(user.id(), restaurantId, itemId, value);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal CurrentUser user, @PathVariable String restaurantId,
                       @PathVariable String itemId) {
        menuService.delete(user.id(), restaurantId, itemId);
    }
}
