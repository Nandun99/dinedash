package com.dinedash.restaurant.repository;

import com.dinedash.restaurant.domain.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends MongoRepository<Restaurant, String> {

    List<Restaurant> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Restaurant> findByIdAndOwnerId(String id, Long ownerId);
}
