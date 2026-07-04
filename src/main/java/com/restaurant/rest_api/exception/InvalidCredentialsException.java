package com.restaurant.rest_api.exception;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException(){
        super("Invalid Credentials");
    }
}
