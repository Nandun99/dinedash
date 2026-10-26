package com.dinedash.delivery.repository;

import com.dinedash.delivery.domain.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DriverRepository extends MongoRepository<Driver, Long> {
}
