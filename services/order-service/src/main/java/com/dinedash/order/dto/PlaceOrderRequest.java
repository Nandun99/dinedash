package com.dinedash.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PlaceOrderRequest(
        @NotBlank String restaurantId,
        @NotEmpty @Size(max = 30) @Valid List<Line> items,
        @NotBlank @Size(max = 150) String contactName,
        @Size(max = 20) String contactPhone,
        @NotBlank @Size(max = 400) String deliveryAddress,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @Size(max = 500) String notes
) {
    public record Line(@NotBlank String menuItemId, @Min(1) @Max(50) int quantity) {
    }
}
