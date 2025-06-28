package com.itimpulse.urlshortener.controller;

import com.itimpulse.urlshortener.exceptions.NotFoundException;
import com.itimpulse.urlshortener.exceptions.UrlExpiredException;
import com.itimpulse.urlshortener.model.ShortenUrl;
import com.itimpulse.urlshortener.service.IUrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping
@Slf4j
@Tag(name = "URL Redirection", description = "Handles redirection from short URLs to original destinations.")
public class UrlRedirectController {

    private final IUrlShortenerService urlShortenerService;

    /**
     * Constructor for dependency injection.
     *
     * @param urlshortenerService Service layer implementation for URL shortening operations
     */
    public UrlRedirectController(IUrlShortenerService urlshortenerService) {
        this.urlShortenerService = urlshortenerService;
    }


    /**
     * Redirects to the original URL using the shortened ID.
     *
     * <p>This endpoint handles the core functionality of URL redirection. When a user visits a
     * shortened URL, this method: 1. Retrieves the original URL from the database using the provided
     * ID 2. Checks if the URL has expired (if TTL was set) 3. Performs an HTTP 302 redirect to the
     * original URL
     *
     * <p>The redirect is performed using HTTP status 302 (Found) which is appropriate for temporary
     * redirects and maintains SEO benefits for the original URL.
     *
     * @param id The shortened URL identifier
     * @return ResponseEntity with 302 redirect status and Location header
     * @throws NotFoundException if the ID doesn't exist in the database
     * @throws UrlExpiredException if the URL has expired based on its TTL
     */
    @Operation(
            summary = "Redirect to original URL",
            description = "Redirects to the original URL using the shortened ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to original URL"),
            @ApiResponse(responseCode = "404", description = "Short URL not found", content = @Content),
            @ApiResponse(responseCode = "410", description = "Short URL expired", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Object> redirectToOriginal(
            @Parameter(description = "Shortened URL identifier") @PathVariable String id) {
        // Retrieve the URL entity (includes expiration check)
        ShortenUrl url = urlShortenerService.getShortUrl(id);

        // Log the redirect for monitoring and analytics purposes
        log.info("Redirecting to: {}", url.getUrl());

        // Perform HTTP 302 redirect to the original URL
        return ResponseEntity.status(302).location(URI.create(url.getUrl())).build();
    }
}
