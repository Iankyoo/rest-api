package com.restaurant.rest_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Set;

public record MenuItemRequest (
        @NotBlank
        String name,
        String description,
        @NotNull
        BigDecimal price,
        @NotNull
        Boolean available,
        @NotNull
        Set<Long> categoryIds
){
}
