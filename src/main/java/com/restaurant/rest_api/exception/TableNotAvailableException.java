package com.restaurant.rest_api.exception;

public class TableNotAvailableException extends RuntimeException{
    public TableNotAvailableException(Long id){
        super("Table is not available with id: " + id);
    }
}
