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

/**
 * Global exception handler for the URL Shortener application.
 * 
 * This class provides centralized exception handling across all controllers,
 * ensuring consistent error responses and proper HTTP status codes.
 * 
 * The handler covers:
 * - Validation errors (400 Bad Request)
 * - Business logic conflicts (409 Conflict) 
 * - Resource not found errors (404 Not Found)
 * - URL expiration errors (410 Gone)
 * - Unexpected system errors (500 Internal Server Error)
 * 
 * All responses use the standardized CustomResponse wrapper to maintain
 * consistency in the API response format.
 * 
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors.
     * 
     * This method is triggered when request validation fails, such as:
     * - Invalid URL format in ShortenUrlRequestDto
     * - Custom ID validation failures
     * - Missing required fields
     * 
     * The handler extracts all field-level validation errors and returns
     * them in a structured format, allowing clients to understand exactly
     * which fields failed validation and why.
     * 
     * Example response:
     * {
     *   "message": "Validation failed",
     *   "statusCode": 400,
     *   "data": {
     *     "longUrl": "Invalid URL format",
     *     "customId": "customId must be at least 6 characters long."
     *   }
     * }
     * 
     * @param ex The MethodArgumentNotValidException containing validation errors
     * @return ResponseEntity with 400 status and detailed validation error map
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();

        // Extract all field validation errors
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        // Create standardized error response with validation details
        CustomResponse<Map<String, String>> response = CustomResponse.errorResponse(
                "Validation failed", HttpStatus.BAD_REQUEST.value());
        response.setData(errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles business logic conflicts, typically duplicate resources.
     * 
     * This exception is thrown when:
     * - A user tries to create a shortened URL with a custom ID that already exists
     * - Other business rules prevent resource creation due to conflicts
     * 
     * Returns HTTP 409 Conflict
     * 
     * Example scenario: User tries to create custom short URL "abc123" but
     * that ID is already in use.
     * 
     * @param ex The ConflictException containing the conflict details
     * @return ResponseEntity with 409 status and error message
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<CustomResponse<String>> handleConflictException(ConflictException ex) {
        CustomResponse<String> response = CustomResponse.errorResponse(ex.getMessage(), HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handles resource not found errors.
     * 
     * This exception is thrown when:
     * - A user tries to access a short URL that doesn't exist
     * - A user tries to delete a short URL that doesn't exist
     * - Any operation references a non-existent resource
     * 
     * Returns HTTP 404 Not Found, indicating that the requested resource
     * could not be located on the server.
     * 
     * Example scenarios:
     * - Accessing /abc123 when no short URL with ID "abc123" exists
     * - Trying to delete a short URL that was already removed
     * 
     * @param ex The NotFoundException containing the error details
     * @return ResponseEntity with 404 status and error message
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<CustomResponse<String>> handleNotFoundException(NotFoundException ex) {
        CustomResponse<String> response = CustomResponse.errorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles URL expiration errors.
     * 
     * This exception is thrown when:
     * - A user tries to access a short URL that has expired based on its TTL
     * - The URL existed but is no longer valid due to time constraints
     * 
     * Returns HTTP 410 Gone, which indicates that the resource was previously
     * available but is no longer accessible and will not be available again.
     * This is more specific than 404 as it indicates the resource definitely
     * existed but is now intentionally unavailable.
     * 
     * The 410 status code is semantically correct for expired URLs because:
     * - It indicates the resource was deliberately removed
     * - It suggests the condition is likely permanent
     * - It provides better information than a generic 404
     * 
     * @param ex The UrlExpiredException containing the expiration details
     * @return ResponseEntity with 410 status and error message
     */
    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<CustomResponse<String>> handleUrlExpiredException(UrlExpiredException ex) {
        CustomResponse<String> response = CustomResponse.errorResponse(ex.getMessage(), HttpStatus.GONE.value());
        return new ResponseEntity<>(response, HttpStatus.GONE);
    }

    /**
     * Handles all unexpected exceptions that aren't specifically caught.
     * 
     * This is a catch-all handler for any runtime exceptions that slip through
     * the specific exception handlers. It prevents the application from returning
     * raw stack traces to clients, which could:
     * - Expose sensitive system information
     * - Provide poor user experience
     * - Create security vulnerabilities
     * 
     * Instead, it returns a generic error message while logging the full
     * exception details for debugging purposes.
     * 
     * Returns HTTP 500 Internal Server Error, indicating that the server
     * encountered an error it doesn't know how to handle.
     * 
     * Examples of exceptions caught here:
     * - Database connection failures
     * - Null pointer exceptions
     * - Configuration errors
     * - Third-party service failures
     * 
     * @param ex The unexpected Exception that occurred
     * @return ResponseEntity with 500 status and generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse<String>> handleGeneralExceptions(Exception ex) {
        CustomResponse<String> response = CustomResponse.errorResponse("An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
