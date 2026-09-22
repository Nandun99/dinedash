package com.dinedash.order.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.security.CurrentUser;
import com.dinedash.common.security.Role;
import com.dinedash.order.client.RestaurantClient;
import com.dinedash.order.client.RestaurantClient.PricedLine;
import com.dinedash.order.client.RestaurantClient.Quote;
import com.dinedash.order.domain.Order;
import com.dinedash.order.dto.OrderResponse;
import com.dinedash.order.dto.PlaceOrderRequest;
import com.dinedash.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final CurrentUser CUSTOMER = new CurrentUser(10L, "nimal@example.com", Role.CUSTOMER);

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RestaurantClient restaurantClient;
    @Mock
    private OrderEvents events;

    @InjectMocks
    private OrderService orderService;

    @Test
    void usesRestaurantPricesNotClientPrices() {
        when(restaurantClient.quote(eq("r1"), anyList())).thenReturn(quote(true, true, "0"));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.placeOrder(CUSTOMER, request());

        assertThat(response.subtotal()).isEqualByComparingTo("2900.00");
        assertThat(response.deliveryFee()).isEqualByComparingTo("250.00");
        assertThat(response.total()).isEqualByComparingTo("3150.00");
        assertThat(response.restaurantName()).isEqualTo("Kottu Corner");
        verify(events).created(any(Order.class));
    }

    @Test
    void rejectsWhenRestaurantIsPaused() {
        when(restaurantClient.quote(eq("r1"), anyList())).thenReturn(quote(false, true, "0"));

        assertThatThrownBy(() -> orderService.placeOrder(CUSTOMER, request()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not taking orders");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void rejectsSoldOutItems() {
        when(restaurantClient.quote(eq("r1"), anyList())).thenReturn(quote(true, false, "0"));

        assertThatThrownBy(() -> orderService.placeOrder(CUSTOMER, request()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Sold out");
    }

    @Test
    void enforcesMinimumOrder() {
        when(restaurantClient.quote(eq("r1"), anyList())).thenReturn(quote(true, true, "5000"));

        assertThatThrownBy(() -> orderService.placeOrder(CUSTOMER, request()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Minimum order");
    }

    private static PlaceOrderRequest request() {
        return new PlaceOrderRequest("r1", List.of(new PlaceOrderRequest.Line("m1", 2)), "Nimal Silva",
                "+94771234567", "12 Galle Road, Colombo 03", 6.9, 79.85, null);
    }

    private static Quote quote(boolean accepting, boolean available, String minimum) {
        return new Quote("r1", "Kottu Corner", 6.91, 79.86, accepting, new BigDecimal("250.00"),
                new BigDecimal(minimum),
                List.of(new PricedLine("m1", "Chicken Kottu", new BigDecimal("1450.00"), 2, available)));
    }
}
