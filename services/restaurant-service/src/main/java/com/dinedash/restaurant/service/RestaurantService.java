package com.dinedash.restaurant.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.error.NotFoundException;
import com.dinedash.restaurant.domain.Address;
import com.dinedash.restaurant.domain.Restaurant;
import com.dinedash.restaurant.dto.RestaurantRequest;
import com.dinedash.restaurant.dto.RestaurantResponse;
import com.dinedash.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    static final int MAX_RESTAURANTS_PER_OWNER = 5;

    private final RestaurantRepository restaurantRepository;

    public List<RestaurantResponse> listForOwner(Long ownerId) {
        return restaurantRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
                .map(RestaurantResponse::from)
                .toList();
    }

    public RestaurantResponse create(Long ownerId, RestaurantRequest request) {
        if (restaurantRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId).size() >= MAX_RESTAURANTS_PER_OWNER) {
            throw new BadRequestException("An owner can list up to " + MAX_RESTAURANTS_PER_OWNER + " restaurants");
        }
        Restaurant restaurant = new Restaurant();
        restaurant.setOwnerId(ownerId);
        apply(restaurant, request);
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Owner {} created restaurant {}", ownerId, saved.getId());
        return RestaurantResponse.from(saved);
    }

    public RestaurantResponse update(Long ownerId, String restaurantId, RestaurantRequest request) {
        Restaurant restaurant = getOwned(ownerId, restaurantId);
        apply(restaurant, request);
        return RestaurantResponse.from(restaurantRepository.save(restaurant));
    }

    public RestaurantResponse setAcceptingOrders(Long ownerId, String restaurantId, boolean accepting) {
        Restaurant restaurant = getOwned(ownerId, restaurantId);
        restaurant.setAcceptingOrders(accepting);
        log.info("Restaurant {} accepting orders: {}", restaurantId, accepting);
        return RestaurantResponse.from(restaurantRepository.save(restaurant));
    }

    public Restaurant getOwned(Long ownerId, String restaurantId) {
        return restaurantRepository.findByIdAndOwnerId(restaurantId, ownerId)
                .orElseThrow(() -> new NotFoundException("Restaurant not found"));
    }

    private static void apply(Restaurant restaurant, RestaurantRequest request) {
        if ((request.opensAt() == null) != (request.closesAt() == null)) {
            throw new BadRequestException("Provide both opening and closing time, or neither");
        }
        restaurant.setName(request.name().trim());
        restaurant.setDescription(request.description());
        restaurant.setCuisines(request.cuisines().stream()
                .map(c -> c.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList());
        restaurant.setPhone(request.phone());
        restaurant.setAddress(new Address(request.address().street(), request.address().city(),
                request.address().postalCode()));
        // GeoJSON points are (longitude, latitude)
        restaurant.setLocation(new GeoJsonPoint(request.longitude(), request.latitude()));
        restaurant.setOpensAt(request.opensAt());
        restaurant.setClosesAt(request.closesAt());
        restaurant.setDeliveryFee(request.deliveryFee());
        restaurant.setMinimumOrder(request.minimumOrder());
        restaurant.setAveragePrepMinutes(request.averagePrepMinutes());
    }
}
