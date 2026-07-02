package com.restaurant.rest_api.exception;

public class OrderNotOpenException extends RuntimeException{
    public OrderNotOpenException(Long id){
        super("Order not open with id: " + id);
    }
}
