package com.restaurant.rest_api.dto;

import com.restaurant.rest_api.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(
        @NotNull
        Role role
) {
}
