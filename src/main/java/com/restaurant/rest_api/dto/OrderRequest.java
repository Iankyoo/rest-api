package com.restaurant.rest_api.dto;

import com.restaurant.rest_api.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
    @NotNull
    Long tableId,
    @NotNull
    Long userId
){
}
