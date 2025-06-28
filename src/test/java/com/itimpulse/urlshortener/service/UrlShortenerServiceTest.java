package com.itimpulse.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.itimpulse.urlshortener.dto.ShortenUrlRequestDto;
import com.itimpulse.urlshortener.dto.ShortenUrlResponseDto;
import com.itimpulse.urlshortener.exceptions.BadRequestException;
import com.itimpulse.urlshortener.exceptions.ConflictException;
import com.itimpulse.urlshortener.exceptions.NotFoundException;
import com.itimpulse.urlshortener.exceptions.UrlExpiredException;
import com.itimpulse.urlshortener.model.ShortenUrl;
import com.itimpulse.urlshortener.repository.ShortenUrlRepository;
import com.itimpulse.urlshortener.util.ShortIdGenerator;
import com.itimpulse.urlshortener.util.UrlBuilder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

  @Mock private ShortenUrlRepository shortenUrlRepository;

  @Mock private ShortIdGenerator shortIdGenerator;

  @Mock private UrlBuilder urlBuilder;

  @Mock private RedisTemplate<String, Object> redisTemplate;

  @Mock private ValueOperations<String, Object> valueOperations;

  @InjectMocks private UrlShortenerService urlShortenerService;

  @Test
  void testCreateShortUrlWithGeneratedIdSuccess() {
    ShortenUrlRequestDto request = new ShortenUrlRequestDto();
    request.setLongUrl("https://example.com");

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
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
  void testCreateShortUrlWithCustomIdSuccess() {

    ShortenUrlRequestDto request = new ShortenUrlRequestDto();
    request.setLongUrl("https://example.com");
    request.setCustomId("myCustomId");

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
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
  void testCreateShortUrlWithInvalidCustomId() {
    ShortenUrlRequestDto request = new ShortenUrlRequestDto();
    request.setLongUrl("https://example.com");
    request.setCustomId("!@#");

    when(shortenUrlRepository.existsById("!@#")).thenReturn(false);

    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> urlShortenerService.createShortUrl(request, 1));

    assertEquals("Invalid custom id", exception.getMessage());
    verify(shortenUrlRepository, never()).save(any());
  }

  @Test
  void testCreateShortUrlWithExistingCustomId() {

    ShortenUrlRequestDto request = new ShortenUrlRequestDto();
    request.setLongUrl("https://example.com");
    request.setCustomId("existingId");

    when(shortenUrlRepository.existsById("existingId")).thenReturn(true);

    ConflictException exception =
        assertThrows(ConflictException.class, () -> urlShortenerService.createShortUrl(request, 1));

    assertEquals(
        "The provided ID already exists. Please choose a different ID.", exception.getMessage());

    verify(shortIdGenerator, never()).generate();
    verify(shortenUrlRepository, never()).save(any());
  }

  @Test
  void testGetShortUrlExpiry() {
    ShortenUrl url = new ShortenUrl();
    url.setId("expired123");
    url.setTtl(LocalDateTime.now().minusHours(1));

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(shortenUrlRepository.findById("expired123")).thenReturn(Optional.of(url));

    assertThrows(
        UrlExpiredException.class,
        () -> {
          urlShortenerService.getShortUrl("expired123");
        });
  }

  @Test
  void testGetShortUrlSuccess() {
    ShortenUrl url = new ShortenUrl();
    url.setId("abc123");
    url.setUrl("https://example.com");
    url.setTtl(LocalDateTime.now().plusHours(1)); // Not expired

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(shortenUrlRepository.findById("abc123")).thenReturn(Optional.of(url));

    ShortenUrl result = urlShortenerService.getShortUrl("abc123");

    assertEquals("https://example.com", result.getUrl());
  }

  @Test
  void testGetShortUrlNotFound() {

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(shortenUrlRepository.findById("unknown")).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> urlShortenerService.getShortUrl("unknown"));

    assertEquals("The provided ID could not be found.", exception.getMessage());
  }

  @Test
  void testDeleteShortUrlSuccess() {
    ShortenUrl url = new ShortenUrl();
    url.setId("abc123");

    when(shortenUrlRepository.findById("abc123")).thenReturn(Optional.of(url));

    urlShortenerService.deleteShortUrl("abc123");

    verify(shortenUrlRepository).deleteById("abc123");
  }

  @Test
  void testDeleteShortUrlNotFound() {
    when(shortenUrlRepository.findById("notFoundId")).thenReturn(Optional.empty());

    NotFoundException exception =
        assertThrows(
            NotFoundException.class, () -> urlShortenerService.deleteShortUrl("notFoundId"));

    assertEquals("The provided ID could not be found.", exception.getMessage());
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
