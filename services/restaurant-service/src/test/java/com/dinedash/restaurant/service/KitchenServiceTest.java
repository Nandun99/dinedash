package com.dinedash.restaurant.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.event.RestaurantOrderUpdatedEvent;
import com.dinedash.common.event.Topics;
import com.dinedash.restaurant.domain.Restaurant;
import com.dinedash.restaurant.domain.RestaurantOrder;
import com.dinedash.restaurant.repository.RestaurantOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KitchenServiceTest {

    @Mock
    private RestaurantOrderRepository repository;
    @Mock
    private RestaurantService restaurantService;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KitchenService kitchenService;

    @Test
    void acceptingPublishesEventWithPrepTime() {
        RestaurantOrder order = order(RestaurantOrder.Status.NEW);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        kitchenService.accept(1L, "r1", 100L, 25);

        ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq(Topics.RESTAURANT_ORDER_UPDATED), eq("100"), event.capture());
        var published = (RestaurantOrderUpdatedEvent) event.getValue();
        assertThat(published.status()).isEqualTo(RestaurantOrderUpdatedEvent.Status.ACCEPTED);
        assertThat(published.prepMinutes()).isEqualTo(25);
        assertThat(order.getStatus()).isEqualTo(RestaurantOrder.Status.ACCEPTED);
    }

    @Test
    void cannotMarkNewOrderReady() {
        order(RestaurantOrder.Status.NEW);

        assertThatThrownBy(() -> kitchenService.markReady(1L, "r1", 100L)).isInstanceOf(BadRequestException.class);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void cannotRejectCancelledOrder() {
        order(RestaurantOrder.Status.CANCELLED);

        assertThatThrownBy(() -> kitchenService.reject(1L, "r1", 100L, "busy")).isInstanceOf(BadRequestException.class);
    }

    private RestaurantOrder order(RestaurantOrder.Status status) {
        when(restaurantService.getOwned(1L, "r1")).thenReturn(new Restaurant());
        RestaurantOrder order = new RestaurantOrder();
        order.setOrderId(100L);
        order.setRestaurantId("r1");
        order.setStatus(status);
        when(repository.findByOrderIdAndRestaurantId(100L, "r1")).thenReturn(Optional.of(order));
        return order;
    }
}
