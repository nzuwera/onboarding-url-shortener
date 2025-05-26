package com.itimpulse.urlshortener.controller;

import com.itimpulse.urlshortener.dto.ShortenUrlRequestDto;
import com.itimpulse.urlshortener.dto.ShortenUrlResponseDto;
import com.itimpulse.urlshortener.model.ShortenUrl;
import com.itimpulse.urlshortener.service.UrlShortenerService;
import com.itimpulse.urlshortener.util.CustomResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * REST Controller for URL shortening operations.
 * 
 * This controller provides endpoints for:
 * - Creating shortened URLs with optional TTL and custom IDs
 * - Redirecting to original URLs via shortened IDs
 * - Deleting shortened URLs
 * 
 * All API endpoints follow RESTful conventions and return standardized responses
 * using the CustomResponse wrapper class.
 * 
 */
@RestController
@RequestMapping
@Slf4j
public class UrlShortenerController {

    private final UrlShortenerService urlshortenerService;

    /**
     * Constructor for dependency injection.
     * 
     * @param urlshortenerService Service layer implementation for URL shortening operations
     */
    public UrlShortenerController(UrlShortenerService urlshortenerService) {
        this.urlshortenerService = urlshortenerService;
    }

    /**
     * Welcome endpoint that returns a greeting message.
     * 
     * This endpoint serves as a health check and welcome message for the service.
     * It can be used to verify that the application is running properly.
     * 
     * @return String welcome message
     */
    @GetMapping("/")
    public String index() {
        return "Welcome to URL - Shortener Service!";
    }

    /**
     * Creates a shortened URL from a long URL.
     * 
     * This endpoint accepts a long URL and optional parameters to create a shortened version.
     * The shortened URL can have:
     * - An optional Time-To-Live (TTL) in hours
     * - An optional custom ID (must meet validation requirements)
     * 
     * If no custom ID is provided, a random 6-character ID will be generated.
     * The TTL, if provided, determines when the shortened URL will expire.
     * 
     * @param ttl Optional time-to-live in hours for the shortened URL
     * @param longUrl Request body containing the long URL and optional custom ID
     * @return ResponseEntity containing the created shortened URL details
     * 
     * @throws ConflictException if the custom ID already exists
     * @throws MethodArgumentNotValidException if validation fails on the request
     */
    @PostMapping("/api/v1/shorten-url")
    public ResponseEntity<CustomResponse<ShortenUrlResponseDto>> shortenUrl(
            @RequestParam(required = false) Integer ttl,
            @RequestBody @Valid ShortenUrlRequestDto longUrl) {

        // Delegate URL shortening to the service layer
        ShortenUrlResponseDto shortUrl = urlshortenerService.createShortUrl(longUrl, ttl);

        // Return success response with HTTP 201 Created status
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(
                CustomResponse.successResponse("Short URL created successfully",
                        HttpStatus.CREATED.value(), shortUrl));
    }

    /**
     * Redirects to the original URL using the shortened ID.
     * 
     * This endpoint handles the core functionality of URL redirection. When a user
     * visits a shortened URL, this method:
     * 1. Retrieves the original URL from the database using the provided ID
     * 2. Checks if the URL has expired (if TTL was set)
     * 3. Performs an HTTP 302 redirect to the original URL
     * 
     * The redirect is performed using HTTP status 302 (Found) which is appropriate
     * for temporary redirects and maintains SEO benefits for the original URL.
     * 
     * @param id The shortened URL identifier
     * @return ResponseEntity with 302 redirect status and Location header
     * 
     * @throws NotFoundException if the ID doesn't exist in the database
     * @throws UrlExpiredException if the URL has expired based on its TTL
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> redirectToOriginal(@PathVariable String id) {
        // Retrieve the URL entity (includes expiration check)
        ShortenUrl url = urlshortenerService.getShortUrl(id);

        // Log the redirect for monitoring and analytics purposes
        log.info("Redirecting to: {}", url.getUrl());
        
        // Perform HTTP 302 redirect to the original URL
        return ResponseEntity.status(302).location(URI.create(url.getUrl())).build();
    }

    /**
     * Deletes a shortened URL by its ID.
     * 
     * This endpoint allows users to permanently remove a shortened URL from the system.
     * Once deleted, the shortened URL will no longer be accessible.
     * 
     * This operation is irreversible and should be used with caution.
     * 
     * @param id The shortened URL identifier to delete
     * @return ResponseEntity with success message
     * 
     * @throws NotFoundException if the ID doesn't exist in the database
     */
    @DeleteMapping("/api/v1/shorten-url/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteShortUrl(@PathVariable String id) {
        // Delegate deletion to the service layer
        urlshortenerService.deleteShortUrl(id);
        
        // Return success response with HTTP 200 OK status
        return ResponseEntity.ok(CustomResponse.successResponse(
                "Shorten url deleted successfully", HttpStatus.OK.value()));
    }
}
