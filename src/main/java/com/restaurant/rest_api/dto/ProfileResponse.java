package com.restaurant.rest_api.dto;

public record ProfileResponse(
        Long id,
        String phone,
        String bio,
        String avatarUrl
) {
}
