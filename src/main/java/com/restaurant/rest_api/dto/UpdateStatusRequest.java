package com.restaurant.rest_api.dto;

import com.restaurant.rest_api.entity.OrderItemStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull
        OrderItemStatus orderItemStatus
) {
}
