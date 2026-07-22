package com.restaurant.rest_api.fixtures;

import com.restaurant.rest_api.entity.Category;
import com.restaurant.rest_api.entity.MenuItem;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class MenuItemFixture {

    public static MenuItem buildMenuItem(){
        Set<Category> categorySet = new HashSet<>();
        categorySet.add(CategoryFixture.buildCategory());
        return MenuItem.builder()
                .id(1L)
                .name("nameTest")
                .description("descriptionTest")
                .price(new BigDecimal("15.00"))
                .available(Boolean.TRUE)
                .categories(categorySet)
                .build();
    }
}
