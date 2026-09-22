package com.dinedash.restaurant.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.error.NotFoundException;
import com.dinedash.common.event.RestaurantOrderUpdatedEvent;
import com.dinedash.common.event.Topics;
import com.dinedash.restaurant.domain.RestaurantOrder;
import com.dinedash.restaurant.domain.RestaurantOrder.Status;
import com.dinedash.restaurant.repository.RestaurantOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KitchenService {

    private final RestaurantOrderRepository repository;
    private final RestaurantService restaurantService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public List<RestaurantOrder> activeOrders(Long ownerId, String restaurantId) {
        restaurantService.getOwned(ownerId, restaurantId);
        return repository.findByRestaurantIdAndStatusInOrderByReceivedAtAsc(restaurantId,
                EnumSet.of(Status.NEW, Status.ACCEPTED));
    }

    public List<RestaurantOrder> recentOrders(Long ownerId, String restaurantId) {
        restaurantService.getOwned(ownerId, restaurantId);
        return repository.findByRestaurantIdOrderByReceivedAtDesc(restaurantId, PageRequest.of(0, 50));
    }

    public RestaurantOrder accept(Long ownerId, String restaurantId, Long orderId, int prepMinutes) {
        RestaurantOrder order = load(ownerId, restaurantId, orderId);
        require(order, Status.NEW, "accept");
        order.setStatus(Status.ACCEPTED);
        order.setPrepMinutes(prepMinutes);
        order.setAcceptedAt(Instant.now());
        RestaurantOrder saved = repository.save(order);
        publish(saved, RestaurantOrderUpdatedEvent.Status.ACCEPTED, null);
        return saved;
    }

    public RestaurantOrder reject(Long ownerId, String restaurantId, Long orderId, String reason) {
        RestaurantOrder order = load(ownerId, restaurantId, orderId);
        require(order, Status.NEW, "reject");
        order.setStatus(Status.REJECTED);
        order.setRejectReason(reason);
        RestaurantOrder saved = repository.save(order);
        publish(saved, RestaurantOrderUpdatedEvent.Status.REJECTED, reason);
        return saved;
    }

    public RestaurantOrder markReady(Long ownerId, String restaurantId, Long orderId) {
        RestaurantOrder order = load(ownerId, restaurantId, orderId);
        require(order, Status.ACCEPTED, "mark ready");
        order.setStatus(Status.READY);
        order.setReadyAt(Instant.now());
        RestaurantOrder saved = repository.save(order);
        publish(saved, RestaurantOrderUpdatedEvent.Status.READY, null);
        return saved;
    }

    private RestaurantOrder load(Long ownerId, String restaurantId, Long orderId) {
        restaurantService.getOwned(ownerId, restaurantId);
        return repository.findByOrderIdAndRestaurantId(orderId, restaurantId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    private static void require(RestaurantOrder order, Status expected, String action) {
        if (order.getStatus() != expected) {
            throw new BadRequestException("Cannot " + action + " an order that is " + order.getStatus());
        }
    }

    private void publish(RestaurantOrder order, RestaurantOrderUpdatedEvent.Status status, String reason) {
        var event = new RestaurantOrderUpdatedEvent(order.getOrderId(), order.getRestaurantId(), status, reason,
                order.getPrepMinutes(), Instant.now());
        kafkaTemplate.send(Topics.RESTAURANT_ORDER_UPDATED, String.valueOf(order.getOrderId()), event);
        log.info("Order {} {} by restaurant {}", order.getOrderId(), status, order.getRestaurantId());
    }
}
