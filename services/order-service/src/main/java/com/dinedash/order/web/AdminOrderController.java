package com.dinedash.order.web;

import com.dinedash.order.domain.Order;
import com.dinedash.order.domain.OrderStatus;
import com.dinedash.order.dto.OrderStats;
import com.dinedash.order.dto.OrderSummary;
import com.dinedash.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private static final ZoneId ZONE = ZoneId.of("Asia/Colombo");

    private final OrderRepository orderRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public Page<OrderSummary> list(@RequestParam(required = false) OrderStatus status,
                                   @PageableDefault(size = 20) Pageable pageable) {
        Page<Order> page = status == null
                ? orderRepository.findAllByOrderByPlacedAtDesc(pageable)
                : orderRepository.findByStatusOrderByPlacedAtDesc(status, pageable);
        return page.map(OrderSummary::from);
    }

    @GetMapping("/stats")
    @Transactional(readOnly = true)
    public OrderStats stats() {
        var since = LocalDate.now(ZONE).atStartOfDay(ZONE).toInstant();
        Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
        orderRepository.countByStatusSince(since).forEach(c -> byStatus.put(c.getStatus(), c.getCount()));
        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        return new OrderStats(total, orderRepository.sumTotalSince(since, OrderStatus.DELIVERED), byStatus);
    }
}
