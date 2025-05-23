package com.itimpulse.urlshortener.exceptions;

import com.itimpulse.urlshortener.util.CustomResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        CustomResponse<Map<String, String>> response = CustomResponse.errorResponse("Validation failed", HttpStatus.BAD_REQUEST.value());
        response.setData(errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<CustomResponse<String>> handleConflictException(ConflictException ex) {
        CustomResponse<String> response = CustomResponse.errorResponse(ex.getMessage(), HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CustomResponse<String>> handleNotFoundException(NotFoundException ex) {
        CustomResponse<String> response = CustomResponse.errorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<CustomResponse<String>> handleUrlExpiredException(UrlExpiredException ex) {
        CustomResponse<String> response = CustomResponse.errorResponse(ex.getMessage(), HttpStatus.GONE.value());
        return new ResponseEntity<>(response, HttpStatus.GONE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse<String>> handleGeneralExceptions(Exception ex) {
        CustomResponse<String> response = CustomResponse.errorResponse("An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
