package com.dinedash.restaurant.domain;

import com.dinedash.common.event.OrderItemLine;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * The kitchen's copy of a paid order. Keyed by order id so a redelivered
 * {@code order.paid} event simply overwrites the same document.
 */
@Document(collection = "restaurant_orders")
@CompoundIndex(name = "restaurant_status", def = "{'restaurantId': 1, 'status': 1, 'receivedAt': -1}")
@Getter
@Setter
@NoArgsConstructor
public class RestaurantOrder {

    @Id
    private Long orderId;

    private String restaurantId;

    private String customerName;

    private List<OrderItemLine> items;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal total;

    private String notes;

    private Status status = Status.NEW;

    private String rejectReason;

    private Integer prepMinutes;

    private Instant receivedAt;

    private Instant acceptedAt;

    private Instant readyAt;

    @Version
    private Long version;

    public enum Status {
        NEW, ACCEPTED, READY, REJECTED, CANCELLED;

        public boolean isActive() {
            return this == NEW || this == ACCEPTED;
        }
    }
}
