package com.dinedash.restaurant.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "menu_items")
@CompoundIndex(name = "restaurant_category", def = "{'restaurantId': 1, 'category': 1}")
@Getter
@Setter
@NoArgsConstructor
public class MenuItem {

    @Id
    private String id;

    private String restaurantId;

    private String name;

    private String description;

    private String category;

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal price;

    private String imageUrl;

    private boolean available = true;

    private boolean vegetarian;

    private boolean spicy;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
