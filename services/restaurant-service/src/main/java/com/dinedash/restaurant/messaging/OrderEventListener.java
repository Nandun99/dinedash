package com.dinedash.restaurant.messaging;

import com.dinedash.common.event.OrderCancelledEvent;
import com.dinedash.common.event.OrderPaidEvent;
import com.dinedash.common.event.Topics;
import com.dinedash.restaurant.domain.RestaurantOrder;
import com.dinedash.restaurant.repository.RestaurantOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final RestaurantOrderRepository repository;

    @KafkaListener(topics = Topics.ORDER_PAID)
    public void onOrderPaid(OrderPaidEvent event) {
        if (repository.existsById(event.orderId())) {
            log.debug("Order {} already in kitchen queue", event.orderId());
            return;
        }
        RestaurantOrder order = new RestaurantOrder();
        order.setOrderId(event.orderId());
        order.setRestaurantId(event.restaurantId());
        order.setCustomerName(event.customerName());
        order.setItems(event.items());
        order.setTotal(event.totalAmount());
        order.setNotes(event.notes());
        order.setReceivedAt(event.paidAt());
        repository.save(order);
        log.info("New order {} queued for restaurant {}", event.orderId(), event.restaurantId());
    }

    @KafkaListener(topics = Topics.ORDER_CANCELLED)
    public void onOrderCancelled(OrderCancelledEvent event) {
        repository.findById(event.orderId()).ifPresent(order -> {
            if (order.getStatus().isActive()) {
                order.setStatus(RestaurantOrder.Status.CANCELLED);
                repository.save(order);
                log.info("Order {} cancelled, removed from kitchen queue", event.orderId());
            }
        });
    }
}
