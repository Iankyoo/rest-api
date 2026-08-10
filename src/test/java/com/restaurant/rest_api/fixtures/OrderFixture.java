package com.restaurant.rest_api.fixtures;

import com.restaurant.rest_api.entity.Order;
import com.restaurant.rest_api.entity.OrderStatus;
import com.restaurant.rest_api.entity.RestaurantTable;
import com.restaurant.rest_api.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderFixture {

    public static Order buildOrder(){
        return Order.builder()
                .id(1L)
                .user(UserFixture.buildUser())
                .restaurantTable(RestaurantTableFixture.buildTable())
                .status(OrderStatus.OPEN)
                .totalPrice(new BigDecimal("10.00"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Order buildOrder(
            Long id, User user, RestaurantTable table, OrderStatus status, BigDecimal totalPrice, LocalDateTime createdAt
    ){
        return Order.builder()
                .id(id)
                .user(user)
                .restaurantTable(table)
                .status(status)
                .totalPrice(totalPrice)
                .createdAt(createdAt)
                .build();
    }
}
