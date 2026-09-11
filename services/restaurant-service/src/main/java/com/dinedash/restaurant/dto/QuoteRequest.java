package com.dinedash.restaurant.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record QuoteRequest(@NotEmpty @Valid List<Line> items) {

    public record Line(@NotBlank String menuItemId, @Min(1) @Max(50) int quantity) {
    }
}
