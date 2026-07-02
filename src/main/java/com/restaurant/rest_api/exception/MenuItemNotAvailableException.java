package com.restaurant.rest_api.exception;

public class MenuItemNotAvailableException extends RuntimeException{
    public MenuItemNotAvailableException(Long id){
        super("MenuItem not available with id: " + id);
    }
}
