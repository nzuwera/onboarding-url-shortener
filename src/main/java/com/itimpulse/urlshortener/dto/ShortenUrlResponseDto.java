package com.itimpulse.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShortenUrlResponseDto {
  @Schema(description = "Shortened URL identifier", example = "abc123")
  private String id;

  @Schema(description = "The original long URL", example = "https://example.com")
  private String url;

  @Schema(
      description = "Expiration date and time for the shortened URL (if set)",
      example = "2024-12-31T23:59:59")
  private LocalDateTime ttl;

  @Schema(description = "The full shortened URL", example = "http://localhost:8080/abc123")
  private String shortenUrl;
}
