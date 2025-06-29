package com.itimpulse.urlshortener.controller;

import com.itimpulse.urlshortener.dto.ShortenUrlRequestDto;
import com.itimpulse.urlshortener.dto.ShortenUrlResponseDto;
import com.itimpulse.urlshortener.exceptions.ConflictException;
import com.itimpulse.urlshortener.exceptions.NotFoundException;
import com.itimpulse.urlshortener.service.IUrlShortenerService;
import com.itimpulse.urlshortener.util.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for URL shortening operations.
 *
 * <p>This controller provides endpoints for: - Creating shortened URLs with optional TTL and custom
 * IDs - Redirecting to original URLs via shortened IDs - Deleting shortened URLs
 *
 * <p>All API endpoints follow RESTful conventions and return standardized responses using the
 * CustomResponse wrapper class.
 */
@RestController
@RequestMapping(value = "/api/v1/url-shortener")
@Slf4j
@Tag(name = "URL Shortener API", description = "Operations related to URL shortening")
public class UrlShortenerController {

  private final IUrlShortenerService urlShortenerService;

  /**
   * Constructor for dependency injection.
   *
   * @param urlshortenerService Service layer implementation for URL shortening operations
   */
  // Injects the URL shortener service to handle business logic.
  public UrlShortenerController(IUrlShortenerService urlshortenerService) {
    this.urlShortenerService = urlshortenerService;
  }

  /**
   * Welcome endpoint that returns a greeting message.
   *
   * <p>This endpoint serves as a health check and welcome message for the service. It can be used
   * to verify that the application is running properly.
   *
   * @return String welcome message
   */
  // Provides a simple health check endpoint.
  @Operation(
      summary = "Welcome endpoint",
      description = "Health check and welcome message for the service.")
  @GetMapping("/")
  public String index() {
    return "Welcome to URL - Shortener Service!";
  }

  /**
   * Creates a shortened URL from a long URL.
   *
   * <p>This endpoint accepts a long URL and optional parameters to create a shortened version. The
   * shortened URL can have: - An optional Time-To-Live (TTL) in hours - An optional custom ID (must
   * meet validation requirements)
   *
   * <p>If no custom ID is provided, a random 6-character ID will be generated. The TTL, if
   * provided, determines when the shortened URL will expire.
   *
   * @param ttl Optional time-to-live in hours for the shortened URL
   * @param longUrl Request body containing the long URL and optional custom ID
   * @return ResponseEntity containing the created shortened URL details
   * @throws ConflictException if the custom ID already exists
   * @throws MethodArgumentNotValidException if validation fails on the request
   */
  @Operation(
      summary = "Create a shortened URL",
      description = "Creates a shortened URL from a long URL with optional TTL and custom ID.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Short URL created successfully",
        content = @Content(schema = @Schema(implementation = ShortenUrlResponseDto.class))),
    @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
    @ApiResponse(responseCode = "409", description = "Custom ID already exists", content = @Content)
  })
  @PostMapping
  public ResponseEntity<CustomResponse<ShortenUrlResponseDto>> shortenUrl(
      @Parameter(description = "Optional time-to-live in hours for the shortened URL")
          @RequestParam(required = false)
          Integer ttl,
      @RequestBody @Valid ShortenUrlRequestDto longUrl) {

    // Delegate the core logic of URL shortening to the service layer.
    ShortenUrlResponseDto shortUrl = urlShortenerService.createShortUrl(longUrl, ttl);

    // Wrap the response in a custom structure and return with a 201 Created status.
    return ResponseEntity.status(HttpStatus.CREATED.value())
        .body(
            CustomResponse.successResponse(
                "Short URL created successfully", HttpStatus.CREATED.value(), shortUrl));
  }


  /**
   * Deletes a shortened URL by its ID.
   *
   * <p>This endpoint allows users to permanently remove a shortened URL from the system. Once
   * deleted, the shortened URL will no longer be accessible.
   *
   * <p>This operation is irreversible and should be used with caution.
   *
   * @param id The shortened URL identifier to delete
   * @return ResponseEntity with success message
   * @throws NotFoundException if the ID doesn't exist in the database
   */
  @Operation(summary = "Delete a shortened URL", description = "Deletes a shortened URL by its ID.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Shorten url deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Short URL not found", content = @Content)
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<CustomResponse<Void>> deleteShortUrl(
      @Parameter(description = "Shortened URL identifier to delete") @PathVariable String id) {
    // Delegate the deletion logic to the service layer.
    urlShortenerService.deleteShortUrl(id);

    // Return a success response with a 200 OK status.
    return ResponseEntity.ok(
        CustomResponse.successResponse("Shorten url deleted successfully", HttpStatus.OK.value()));
  }
}
