package com.restaurant.rest_api.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileRequest (
        @NotBlank
        String phone,
        String bio,
        String avatarUrl
){
}
