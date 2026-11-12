package com.dinedash.restaurant.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "restaurants")
@Getter
@Setter
@NoArgsConstructor
public class Restaurant {

    public static final ZoneId ZONE = ZoneId.of("Asia/Colombo");

    @Id
    private String id;

    @Indexed
    private Long ownerId;

    private String name;

    private String description;

    @Indexed
    private List<String> cuisines = new ArrayList<>();

    private String phone;

    private Address address;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;

    private String logoUrl;

    private String coverImageUrl;

    private LocalTime opensAt;

    private LocalTime closesAt;

    /** New restaurants need admin approval before they appear to customers. */
    @Indexed
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING_APPROVAL;

    /** Manual switch so owners can pause orders, e.g. when the kitchen is busy. */
    private boolean acceptingOrders = true;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal minimumOrder = BigDecimal.ZERO;

    private int averagePrepMinutes = 20;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private Long version;

    public boolean isOpenAt(ZonedDateTime time) {
        return isOpenAt(opensAt, closesAt, time);
    }

    public static boolean isOpenAt(LocalTime opensAt, LocalTime closesAt, ZonedDateTime time) {
        if (opensAt == null || closesAt == null) {
            return true;
        }
        LocalTime now = time.withZoneSameInstant(ZONE).toLocalTime();
        // Handles places that close after midnight, e.g. 18:00 - 02:00
        return opensAt.isBefore(closesAt)
                ? !now.isBefore(opensAt) && now.isBefore(closesAt)
                : !now.isBefore(opensAt) || now.isBefore(closesAt);
    }

    public boolean isVisible() {
        // Documents created before approvals existed have no status and count as active
        return approvalStatus == null || approvalStatus == ApprovalStatus.ACTIVE;
    }

    public boolean canTakeOrders(ZonedDateTime time) {
        return isVisible() && acceptingOrders && isOpenAt(time);
    }

    public enum ApprovalStatus {
        PENDING_APPROVAL, ACTIVE, SUSPENDED
    }
}
