package com.itimpulse.urlshortener.service;

import com.itimpulse.urlshortener.dto.ShortenUrlRequestDto;
import com.itimpulse.urlshortener.dto.ShortenUrlResponseDto;
import com.itimpulse.urlshortener.model.ShortenUrl;

public interface UrlShortenerService {
    ShortenUrlResponseDto createShortUrl(ShortenUrlRequestDto longUrl, Integer ttl);
    ShortenUrl getShortUrl(String shortId);
    void deleteShortUrl(String shortId);
}
