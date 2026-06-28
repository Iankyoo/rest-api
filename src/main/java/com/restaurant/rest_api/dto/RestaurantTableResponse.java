package com.restaurant.rest_api.dto;

import com.restaurant.rest_api.entity.TableStatus;

public record RestaurantTableResponse(
        Long id,
        Integer number,
        Integer capacity,
        TableStatus status
) {
}
