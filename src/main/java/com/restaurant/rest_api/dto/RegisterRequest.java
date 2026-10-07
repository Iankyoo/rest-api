package com.restaurant.rest_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        String name,
        @NotBlank
        @Email
        String email,
        @NotBlank
        @Size(min = 8, message = "Password must have at least 8 characters")
        String password
) {
}
