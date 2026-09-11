package com.dinedash.order.service;

import com.dinedash.common.error.BadRequestException;
import com.dinedash.common.error.NotFoundException;
import com.dinedash.common.security.CurrentUser;
import com.dinedash.common.security.Role;
import com.dinedash.order.client.RestaurantClient;
import com.dinedash.order.client.RestaurantClient.PricedLine;
import com.dinedash.order.client.RestaurantClient.Quote;
import com.dinedash.order.client.RestaurantClient.QuoteLine;
import com.dinedash.order.domain.Order;
import com.dinedash.order.domain.OrderItem;
import com.dinedash.order.dto.OrderResponse;
import com.dinedash.order.dto.OrderSummary;
import com.dinedash.order.dto.PlaceOrderRequest;
import com.dinedash.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantClient restaurantClient;

    @Transactional
    public OrderResponse placeOrder(CurrentUser customer, PlaceOrderRequest request) {
        List<QuoteLine> lines = request.items().stream()
                .map(l -> new QuoteLine(l.menuItemId(), l.quantity()))
                .toList();
        Quote quote = restaurantClient.quote(request.restaurantId(), lines);
        if (!quote.acceptingOrders()) {
            throw new BadRequestException(quote.restaurantName() + " is not taking orders right now");
        }
        List<String> soldOut = quote.items().stream().filter(i -> !i.available()).map(PricedLine::name).toList();
        if (!soldOut.isEmpty()) {
            throw new BadRequestException("Sold out: " + String.join(", ", soldOut));
        }

        Order order = new Order();
        order.setCustomerId(customer.id());
        order.setCustomerName(request.contactName().trim());
        order.setCustomerPhone(request.contactPhone());
        order.setRestaurantId(quote.restaurantId());
        order.setRestaurantName(quote.restaurantName());
        order.setDeliveryAddress(request.deliveryAddress().trim());
        order.setDropoffLat(request.latitude());
        order.setDropoffLng(request.longitude());
        order.setPickupLat(quote.latitude());
        order.setPickupLng(quote.longitude());
        order.setNotes(request.notes());
        quote.items().forEach(i -> order.addItem(new OrderItem(i.menuItemId(), i.name(), i.unitPrice(), i.quantity())));

        BigDecimal subtotal = order.getItems().stream().map(OrderItem::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (subtotal.compareTo(quote.minimumOrder()) < 0) {
            throw new BadRequestException("Minimum order for this restaurant is LKR " + quote.minimumOrder());
        }
        order.setSubtotal(subtotal);
        order.setDeliveryFee(quote.deliveryFee());
        order.setTotal(subtotal.add(quote.deliveryFee()));

        Order saved = orderRepository.save(order);
        log.info("Order {} placed by customer {} at restaurant {} for {}", saved.getId(), customer.id(),
                saved.getRestaurantId(), saved.getTotal());
        return OrderResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummary> myOrders(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdOrderByPlacedAtDesc(customerId, pageable).map(OrderSummary::from);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(CurrentUser user, Long orderId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        boolean allowed = user.is(Role.ADMIN)
                || Objects.equals(order.getCustomerId(), user.id())
                || (user.is(Role.DRIVER) && Objects.equals(order.getDriverId(), user.id()));
        if (!allowed) {
            // Do not reveal that the order exists
            throw new NotFoundException("Order not found");
        }
        return OrderResponse.from(order);
    }
}
