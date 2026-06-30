package com.restaurant.rest_api.exception;

public class RestaurantTableNotFoundException extends RuntimeException{
    public RestaurantTableNotFoundException(Long id){
        super("Table not found with id: " + id);
    }
}
