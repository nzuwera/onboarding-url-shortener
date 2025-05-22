package com.itimpulse.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShortenUrlResponseDto {
    private String id;
    private String url;
    private LocalDateTime ttl;
    private String shortenUrl;

}
