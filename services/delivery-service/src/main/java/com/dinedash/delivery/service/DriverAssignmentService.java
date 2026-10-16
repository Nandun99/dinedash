package com.dinedash.delivery.service;

import com.dinedash.common.event.DeliveryUpdatedEvent;
import com.dinedash.delivery.config.AssignmentProperties;
import com.dinedash.delivery.domain.Delivery;
import com.dinedash.delivery.domain.Driver;
import com.dinedash.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Picks the nearest available driver to the restaurant. Claiming a driver is a single
 * atomic findAndModify (AVAILABLE -> BUSY), so two deliveries can never grab the same driver,
 * even with several service instances running.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DriverAssignmentService {

    private static final int CANDIDATES = 10;

    private final MongoTemplate mongoTemplate;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryEvents events;
    private final AssignmentProperties properties;

    public boolean tryAssign(Delivery delivery) {
        if (delivery.getStatus() != Delivery.Status.SEARCHING) {
            return false;
        }
        Optional<Driver> driver = nearbyCandidates(delivery).stream()
                .map(candidate -> claim(candidate, delivery))
                .flatMap(Optional::stream)
                .findFirst();
        if (driver.isEmpty()) {
            if (!delivery.isNoDriverNotified()) {
                delivery.setNoDriverNotified(true);
                deliveryRepository.save(delivery);
                events.publish(delivery, DeliveryUpdatedEvent.Status.NO_DRIVER_AVAILABLE);
            }
            log.info("No driver available yet for order {}", delivery.getOrderId());
            return false;
        }
        Driver d = driver.get();
        delivery.setDriverId(d.getId());
        delivery.setDriverName(d.getName());
        delivery.setStatus(Delivery.Status.ASSIGNED);
        delivery.setAssignedAt(Instant.now());
        deliveryRepository.save(delivery);
        events.publish(delivery, DeliveryUpdatedEvent.Status.ASSIGNED);
        log.info("Assigned driver {} to order {}", d.getId(), delivery.getOrderId());
        return true;
    }

    /** Deliveries that found no driver are retried as drivers come online. */
    @Scheduled(fixedDelayString = "${dinedash.assignment.retry-interval}")
    public void retryUnassigned() {
        for (Delivery delivery : deliveryRepository.findByStatusOrderByReadyAtAsc(Delivery.Status.SEARCHING)) {
            tryAssign(delivery);
        }
    }

    List<Driver> nearbyCandidates(Delivery delivery) {
        Query available = new Query(Criteria.where("status").is(Driver.Status.AVAILABLE)
                .and("lastSeenAt").gte(Instant.now().minus(properties.driverStaleAfter())));
        NearQuery near = NearQuery.near(delivery.getPickup(), Metrics.KILOMETERS)
                .spherical(true)
                .maxDistance(new Distance(properties.searchRadiusKm(), Metrics.KILOMETERS))
                .query(available)
                .limit(CANDIDATES);
        return mongoTemplate.geoNear(near, Driver.class).getContent().stream().map(GeoResult::getContent).toList();
    }

    private Optional<Driver> claim(Driver candidate, Delivery delivery) {
        Query query = new Query(Criteria.where("_id").is(candidate.getId()).and("status").is(Driver.Status.AVAILABLE));
        Update update = new Update().set("status", Driver.Status.BUSY).set("currentDeliveryId", delivery.getId());
        return Optional.ofNullable(mongoTemplate.findAndModify(query, update,
                FindAndModifyOptions.options().returnNew(true), Driver.class));
    }
}
