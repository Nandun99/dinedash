package com.dinedash.restaurant.repository;

import com.dinedash.restaurant.domain.RestaurantOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RestaurantOrderRepository extends MongoRepository<RestaurantOrder, Long> {

    List<RestaurantOrder> findByRestaurantIdAndStatusInOrderByReceivedAtAsc(String restaurantId,
                                                                           Collection<RestaurantOrder.Status> statuses);

    List<RestaurantOrder> findByRestaurantIdOrderByReceivedAtDesc(String restaurantId, Pageable pageable);

    Optional<RestaurantOrder> findByOrderIdAndRestaurantId(Long orderId, String restaurantId);
}
