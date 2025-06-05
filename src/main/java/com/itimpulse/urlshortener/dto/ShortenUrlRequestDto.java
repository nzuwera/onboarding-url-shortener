package com.itimpulse.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ShortenUrlRequestDto {

    @URL(message = "Invalid URL format")
    @NotBlank(message = "Long URL is required")
    private String longUrl;

    private String customId;
}
