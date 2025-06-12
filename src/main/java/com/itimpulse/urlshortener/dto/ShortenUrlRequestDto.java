package com.itimpulse.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ShortenUrlRequestDto {

  @Schema(description = "The original long URL to be shortened", example = "https://example.com")
  @URL(message = "Invalid URL format")
  @NotBlank(message = "Long URL is required")
  private String longUrl;

  @Schema(
      description =
          "Optional custom ID for the shortened URL. Must be at least 6 characters, contain letters and digits.",
      example = "abc123")
  private String customId;
}
