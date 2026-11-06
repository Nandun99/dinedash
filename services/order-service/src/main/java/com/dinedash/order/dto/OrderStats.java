package com.dinedash.order.dto;

import com.dinedash.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.util.Map;

public record OrderStats(long ordersToday, BigDecimal deliveredRevenueToday, Map<OrderStatus, Long> byStatus) {
}
