package com.dinedash.restaurant.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MenuItemRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @NotBlank @Size(max = 60) String category,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 7, fraction = 2) BigDecimal price,
        boolean vegetarian,
        boolean spicy
) {
}
