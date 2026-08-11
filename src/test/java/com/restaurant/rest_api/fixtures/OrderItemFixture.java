package com.restaurant.rest_api.fixtures;

import com.restaurant.rest_api.entity.MenuItem;
import com.restaurant.rest_api.entity.Order;
import com.restaurant.rest_api.entity.OrderItem;
import com.restaurant.rest_api.entity.OrderItemStatus;

import java.math.BigDecimal;

public class OrderItemFixture {

    public static OrderItem buildOrderItem(){
        return OrderItem.builder()
                .id(1L)
                .order(OrderFixture.buildOrder())
                .menuItem(MenuItemFixture.buildMenuItem())
                .quantity(1)
                .observation(null)
                .unitPrice(new BigDecimal("10.00"))
                .orderItemStatus(OrderItemStatus.READY)
                .build();
    }

    public static OrderItem buildOrderItem(Long id, Order order, MenuItem menuItem, Integer quantity, BigDecimal unitPrice, String observation, OrderItemStatus status){
        return OrderItem.builder()
                .id(id)
                .order(order)
                .menuItem(menuItem)
                .quantity(quantity)
                .observation(observation)
                .unitPrice(unitPrice)
                .orderItemStatus(status)
                .build();
    }

}
