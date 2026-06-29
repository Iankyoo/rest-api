package com.restaurant.rest_api.dto;

import com.restaurant.rest_api.entity.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role
) {
}
