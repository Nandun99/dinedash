package com.dinedash.restaurant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record RestaurantRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description,
        @NotEmpty @Size(max = 5) List<@NotBlank String> cuisines,
        @NotBlank String phone,
        @Valid @NotNull AddressDto address,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        LocalTime opensAt,
        LocalTime closesAt,
        @NotNull @PositiveOrZero BigDecimal deliveryFee,
        @NotNull @PositiveOrZero BigDecimal minimumOrder,
        @Min(5) @Max(120) int averagePrepMinutes
) {
    public record AddressDto(@NotBlank String street, @NotBlank String city, String postalCode) {
    }
}
