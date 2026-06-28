package com.restaurant.rest_api.dto;

public record CategoryResponse(
        Long id,
        String name,
        String description
) {
}
