package com.dinedash.common.event;

import java.math.BigDecimal;

public record OrderItemLine(String menuItemId, String name, int quantity, BigDecimal unitPrice) {
}
