package com.restaurant.rest_api.fixtures;

import com.restaurant.rest_api.entity.RestaurantTable;
import com.restaurant.rest_api.entity.TableStatus;

public class RestaurantTableFixture {

    public static RestaurantTable buildTable(){
        return RestaurantTable.builder()
                .id(1L)
                .number(1)
                .status(TableStatus.AVAILABLE)
                .capacity(8)
                .build();
    }

    public static RestaurantTable buildTable(
            Long id, Integer number, TableStatus status, Integer capacity
    ){
        return RestaurantTable.builder()
                .id(id)
                .number(number)
                .status(status)
                .capacity(capacity)
                .build();
    }
}
