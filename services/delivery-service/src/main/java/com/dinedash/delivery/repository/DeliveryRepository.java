package com.dinedash.delivery.repository;

import com.dinedash.delivery.domain.Delivery;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends MongoRepository<Delivery, String> {

    Optional<Delivery> findByOrderId(Long orderId);

    List<Delivery> findByStatusOrderByReadyAtAsc(Delivery.Status status);

    List<Delivery> findTop20ByDriverIdOrderByAssignedAtDesc(Long driverId);
}
