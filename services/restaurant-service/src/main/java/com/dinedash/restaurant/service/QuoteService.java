package com.dinedash.restaurant.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.error.NotFoundException;
import com.dinedash.restaurant.domain.MenuItem;
import com.dinedash.restaurant.domain.Restaurant;
import com.dinedash.restaurant.dto.QuoteRequest;
import com.dinedash.restaurant.dto.QuoteResponse;
import com.dinedash.restaurant.repository.MenuItemRepository;
import com.dinedash.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public QuoteResponse quote(String restaurantId, QuoteRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
        List<String> ids = request.items().stream().map(QuoteRequest.Line::menuItemId).distinct().toList();
        Map<String, MenuItem> found = menuItemRepository.findByRestaurantIdAndIdIn(restaurantId, ids).stream()
                .collect(Collectors.toMap(MenuItem::getId, Function.identity()));
        if (found.size() != ids.size()) {
            throw new BadRequestException("One or more items are not on this restaurant's menu");
        }
        List<QuoteResponse.PricedLine> lines = request.items().stream()
                .map(line -> {
                    MenuItem item = found.get(line.menuItemId());
                    return new QuoteResponse.PricedLine(item.getId(), item.getName(), item.getPrice(),
                            line.quantity(), item.isAvailable());
                })
                .toList();
        return new QuoteResponse(restaurant.getId(), restaurant.getName(), restaurant.getLocation().getY(),
                restaurant.getLocation().getX(), restaurant.canTakeOrders(ZonedDateTime.now()),
                restaurant.getDeliveryFee(), restaurant.getMinimumOrder(), lines);
    }
}
