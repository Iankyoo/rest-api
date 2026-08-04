package com.restaurant.rest_api.dto;

import jakarta.validation.constraints.NotNull;

public record OrderRequest(
    @NotNull
    Long tableId
){
}
