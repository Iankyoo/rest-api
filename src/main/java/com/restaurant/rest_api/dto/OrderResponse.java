package com.restaurant.rest_api.dto;

import com.restaurant.rest_api.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        Long tableId,
        Long userId,
        List<OrderItemResponse> items
) {
}
