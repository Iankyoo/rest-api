package com.restaurant.rest_api.dto;

import com.restaurant.rest_api.entity.Category;

import java.math.BigDecimal;
import java.util.Set;

public record MenuItemResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Boolean available,
        Set<CategoryResponse> categories
) {
}
