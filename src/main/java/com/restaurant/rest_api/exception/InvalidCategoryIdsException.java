package com.restaurant.rest_api.exception;

public class InvalidCategoryIdsException extends RuntimeException{
    public InvalidCategoryIdsException(String message){
        super(message);
    }
}
