package com.dinedash.restaurant.repository;

import com.dinedash.restaurant.domain.MenuItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends MongoRepository<MenuItem, String> {

    List<MenuItem> findByRestaurantIdOrderByCategoryAscNameAsc(String restaurantId);

    List<MenuItem> findByRestaurantIdAndAvailableTrueOrderByCategoryAscNameAsc(String restaurantId);

    List<MenuItem> findByRestaurantIdAndIdIn(String restaurantId, Collection<String> ids);

    Optional<MenuItem> findByIdAndRestaurantId(String id, String restaurantId);

    void deleteByRestaurantId(String restaurantId);
}
