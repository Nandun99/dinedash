package com.dinedash.restaurant.service;

import com.dinedash.common.error.NotFoundException;
import com.dinedash.restaurant.cache.CatalogCacheEvictor;
import com.dinedash.restaurant.domain.MenuItem;
import com.dinedash.restaurant.domain.Restaurant;
import com.dinedash.restaurant.dto.MenuItemRequest;
import com.dinedash.restaurant.repository.MenuItemRepository;
import com.dinedash.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private RestaurantService restaurantService;
    @Mock
    private CatalogCacheEvictor cacheEvictor;

    @InjectMocks
    private MenuService menuService;

    @Test
    void ownerCanAddItemToOwnRestaurant() {
        when(restaurantService.getOwned(1L, "r1")).thenReturn(new Restaurant());
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = menuService.create(1L, "r1",
                new MenuItemRequest(" Chicken Kottu ", "Classic", "Kottu", new BigDecimal("1450.00"), false, true));

        assertThat(response.name()).isEqualTo("Chicken Kottu");
        assertThat(response.restaurantId()).isEqualTo("r1");
        assertThat(response.available()).isTrue();
        verify(cacheEvictor).menuChanged("r1");
    }

    @Test
    void ownerCannotEditAnotherOwnersMenu() {
        when(restaurantService.getOwned(2L, "r1")).thenThrow(new NotFoundException("Restaurant not found"));

        assertThatThrownBy(() -> menuService.delete(2L, "r1", "m1")).isInstanceOf(NotFoundException.class);
        verify(menuItemRepository, never()).delete(any());
    }

    @Test
    void publicMenuOfUnknownRestaurantIs404() {
        when(restaurantRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> menuService.publicMenu("missing")).isInstanceOf(NotFoundException.class);
    }
}
