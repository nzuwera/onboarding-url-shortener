package com.itimpulse.urlshortener.service;

import com.itimpulse.urlshortener.model.ShortenUrl;

public interface UrlShortenerService {
    public ShortenUrl createShortUrl(String longUrl, String customId, Long ttlHours);

    public String getLongUrl(String shortId);

    public void deleteUrl(String shortId);

    public String generateShortId();
}
