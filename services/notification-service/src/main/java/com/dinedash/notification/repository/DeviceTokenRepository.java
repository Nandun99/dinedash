package com.dinedash.notification.repository;

import com.dinedash.notification.domain.DeviceToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DeviceTokenRepository extends MongoRepository<DeviceToken, String> {

    List<DeviceToken> findByUserId(Long userId);
}
