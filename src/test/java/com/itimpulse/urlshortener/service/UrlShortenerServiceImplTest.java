package com.itimpulse.urlshortener.service;

import com.itimpulse.urlshortener.dto.ShortenUrlRequestDto;
import com.itimpulse.urlshortener.dto.ShortenUrlResponseDto;
import com.itimpulse.urlshortener.exceptions.ConflictException;
import com.itimpulse.urlshortener.exceptions.UrlExpiredException;
import com.itimpulse.urlshortener.model.ShortenUrl;
import com.itimpulse.urlshortener.repository.ShortenUrlRepository;
import com.itimpulse.urlshortener.util.ShortIdGenerator;
import com.itimpulse.urlshortener.util.UrlBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceImplTest {

    @Mock
    private ShortenUrlRepository shortenUrlRepository;

    @Mock
    private ShortIdGenerator shortIdGenerator;

    @Mock
    private UrlBuilder urlBuilder;

    @InjectMocks
    private UrlShortenerServiceImpl urlShortenerService;

    @Test
    void testCreateShortUrlWithGeneratedIdSuccess() {
        ShortenUrlRequestDto request = new ShortenUrlRequestDto();
        request.setLongUrl("https://example.com");

        when(shortIdGenerator.generate()).thenReturn("abc123");
        when(shortenUrlRepository.existsById("abc123")).thenReturn(false);
        when(urlBuilder.buildShortUrl("abc123")).thenReturn("http://short.ly/abc123");

        ShortenUrlResponseDto result = urlShortenerService.createShortUrl(request, 1);

        assertEquals("abc123", result.getId());
        assertEquals("https://example.com", result.getUrl());
        assertEquals("http://short.ly/abc123", result.getShortenUrl());

        verify(shortenUrlRepository).save(any(ShortenUrl.class));

    }

    @Test
    void createShortUrlWithCustomIdSuccess() {

        ShortenUrlRequestDto request = new ShortenUrlRequestDto();
        request.setLongUrl("https://example.com");
        request.setCustomId("myCustomId");

        when(shortenUrlRepository.existsById("myCustomId")).thenReturn(false);
        when(urlBuilder.buildShortUrl("myCustomId")).thenReturn("http://short.ly/myCustomId");

        ShortenUrlResponseDto result = urlShortenerService.createShortUrl(request, 2);

        assertEquals("myCustomId", result.getId());
        assertEquals("https://example.com", result.getUrl());
        assertEquals("http://short.ly/myCustomId", result.getShortenUrl());

        verify(shortIdGenerator, never()).generate();
        verify(shortenUrlRepository).save(any(ShortenUrl.class));
    }

    @Test
    void testCreateShortUrlWithExistingCustomId() {

        ShortenUrlRequestDto request = new ShortenUrlRequestDto();
        request.setLongUrl("https://example.com");
        request.setCustomId("existingId");

        when(shortenUrlRepository.existsById("existingId")).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> urlShortenerService.createShortUrl(request, 1)
        );

        assertEquals("The provided ID already exists. Please choose a different ID.", exception.getMessage());

        verify(shortIdGenerator, never()).generate();
        verify(shortenUrlRepository, never()).save(any());
    }

    @Test
    void testGetShortUrl() {
        ShortenUrl url = new ShortenUrl();
        url.setId("expired123");
        url.setTtl(LocalDateTime.now().minusHours(1));

        when(shortenUrlRepository.findById("expired123"))
                .thenReturn(Optional.of(url));

        assertThrows(UrlExpiredException.class, () -> {
            urlShortenerService.getShortUrl("expired123");
        });
    }

    @Test
    void testDeleteShortUrl() {
        ShortenUrl expired1 = new ShortenUrl();
        expired1.setId("id1");
        expired1.setTtl(LocalDateTime.now().minusHours(2));

        ShortenUrl expired2 = new ShortenUrl();
        expired2.setId("id2");
        expired2.setTtl(LocalDateTime.now().minusMinutes(30));

        when(shortenUrlRepository.findByTtlBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(expired1, expired2));

        urlShortenerService.deleteExpiredShortUrls();

        verify(shortenUrlRepository).delete(expired1);
        verify(shortenUrlRepository).delete(expired2);
    }
}