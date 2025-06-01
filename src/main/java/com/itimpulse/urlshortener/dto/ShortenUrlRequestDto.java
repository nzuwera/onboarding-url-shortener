package com.itimpulse.urlshortener.dto;

import com.itimpulse.urlshortener.validations.ValidShortenUrlId;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
public class ShortenUrlRequestDto {

    @URL(message = "Invalid URL format")
    @NotBlank(message = "Long URL is required")
    private String longUrl;

    private String customId;
}
