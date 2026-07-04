package com.restaurant.rest_api.dto;

import com.restaurant.rest_api.entity.Role;

public record AuthResponse(
        String token,
        Long userId,
        String name,
        String email,
        Role role
) {
}