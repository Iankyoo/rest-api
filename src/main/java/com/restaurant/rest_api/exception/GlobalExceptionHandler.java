package com.restaurant.rest_api.exception;

import com.restaurant.rest_api.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validationHandler(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> unreadableMessageHandler(HttpMessageNotReadableException ex){
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Malformed request body or invalid field value"));
    }

    @ExceptionHandler({
            CategoryNotFoundException.class,
            MenuItemNotFoundException.class,
            OrderItemNotFoundException.class,
            OrderNotFoundException.class,
            UserNotFoundException.class,
            RestaurantTableNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> notFoundHandler(RuntimeException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            InvalidCategoryIdsException.class,
            OrderNotOpenException.class,
            MenuItemNotAvailableException.class
    })
    public ResponseEntity<ErrorResponse> badRequestHandler(RuntimeException ex){
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> unauthorizedHandler(RuntimeException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({
            EmailAlreadyExistsException.class,
            TableNotAvailableException.class
    })
    public ResponseEntity<ErrorResponse> conflictHandler(RuntimeException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataIntegrityHandler(DataIntegrityViolationException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Operation violates data integrity: resource is in use or duplicated"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> genericExceptionHandler(Exception ex){
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error"));
    }

}
