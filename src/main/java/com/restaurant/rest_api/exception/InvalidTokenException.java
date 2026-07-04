package com.restaurant.rest_api.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String email) {
        super("Invalid token: user not found for email " + email);
    }
}