package com.restaurant.rest_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull
        Long menuItemId,
        @NotNull
        @Positive
        Integer quantity,
        String observation
) {
}
