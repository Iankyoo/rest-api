package com.restaurant.rest_api.dto;

import com.restaurant.rest_api.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
