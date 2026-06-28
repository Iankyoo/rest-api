package com.restaurant.rest_api.dto;

import com.restaurant.rest_api.entity.TableStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RestaurantTableRequest(
        @NotNull
        @Positive
        Integer number,
        @NotNull
        @Positive
        Integer capacity,
        TableStatus status
) {
}
