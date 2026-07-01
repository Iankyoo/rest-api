package com.restaurant.rest_api.exception;

public class OrderNotFoundException extends RuntimeException{
    public OrderNotFoundException(Long id){
        super("Order not found with id: " + id);
    }
}
