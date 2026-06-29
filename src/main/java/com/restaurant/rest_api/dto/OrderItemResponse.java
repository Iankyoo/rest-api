package com.restaurant.rest_api.dto;

import com.restaurant.rest_api.entity.OrderItemStatus;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long menuItemId,
        String menuItemName,
        Integer quantity,
        BigDecimal unitPrice,
        String observation,
        OrderItemStatus orderItemStatus
) {
}
