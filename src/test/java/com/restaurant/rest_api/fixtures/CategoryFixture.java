package com.restaurant.rest_api.fixtures;

import com.restaurant.rest_api.entity.Category;

public class CategoryFixture {
    public static Category buildCategory(){
        return Category.builder()
                .id(1L)
                .name("nameTest")
                .description("descriptionTest")
                .build();
    }

    public static Category buildCategory(Long id, String name, String description){
        return Category.builder()
                .id(id)
                .name(name)
                .description(description)
                .build();
    }
}
