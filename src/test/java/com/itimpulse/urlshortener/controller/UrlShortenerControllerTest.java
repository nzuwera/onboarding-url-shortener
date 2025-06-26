package com.itimpulse.urlshortener.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itimpulse.urlshortener.config.JpaAuditingConfig;
import com.itimpulse.urlshortener.dto.ShortenUrlRequestDto;
import com.itimpulse.urlshortener.dto.ShortenUrlResponseDto;
import com.itimpulse.urlshortener.exceptions.BadRequestException;
import com.itimpulse.urlshortener.exceptions.ConflictException;
import com.itimpulse.urlshortener.service.UrlShortenerService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = UrlShortenerController.class,
    excludeAutoConfiguration = {JpaAuditingConfig.class})
class UrlShortenerControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UrlShortenerService urlShortenerService;

  @Autowired private ObjectMapper objectMapper;

  @Mock private RedisTemplate<String, Object> redisTemplate;

  @Test
  void testCreateShortenUrl() throws Exception {
    int requestedTtl = 1;
    LocalDateTime ttl = LocalDateTime.now().minusSeconds(requestedTtl);
    String longUrl = "http://long-url.com";

    ShortenUrlRequestDto requestDto = new ShortenUrlRequestDto();

    ShortenUrlResponseDto responseDto =
        new ShortenUrlResponseDto("abc123", longUrl, ttl, "http://url-shortener/abc123");

    requestDto.setLongUrl(longUrl);

    when(urlShortenerService.createShortUrl(requestDto, requestedTtl)).thenReturn(responseDto);

    mockMvc
        .perform(
            post("/api/v1/url-shortener")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("Short URL created successfully"))
        .andExpect(jsonPath("$.statusCode").value(201));
  }

  @Test
  void testWhenCustomIdAlreadyExists() throws Exception {
    ShortenUrlRequestDto requestDto = new ShortenUrlRequestDto();
    requestDto.setLongUrl("https://example.com");
    requestDto.setCustomId("duplicateId");

    when(urlShortenerService.createShortUrl(any(), any()))
        .thenThrow(
            new ConflictException("The provided ID already exists. Please choose a different ID."));

    mockMvc
        .perform(
            post("/api/v1/url-shortener")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.message")
                .value("The provided ID already exists. Please choose a different ID."))
        .andExpect(jsonPath("$.statusCode").value(409));
  }

  @Test
  void whenCustomIdNotAlphanumeric_thenReturnsBadRequest() throws Exception {

    ShortenUrlRequestDto requestDto = new ShortenUrlRequestDto();
    requestDto.setLongUrl("https://example.com");
    requestDto.setCustomId("invalid-id");

    String jsonRequest = new ObjectMapper().writeValueAsString(requestDto);

    when(urlShortenerService.createShortUrl(any(), any()))
        .thenThrow(new BadRequestException("Invalid custom id"));

    mockMvc
        .perform(
            post("/api/v1/url-shortener")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid custom id"))
        .andExpect(jsonPath("$.statusCode").value(400));
  }

  @Test
  void testShortenUrl() throws Exception {
    int requestedTtl = 1;
    LocalDateTime ttl = LocalDateTime.now().minusSeconds(requestedTtl);
    String longUrl = "http://long-url.com";

    ShortenUrlRequestDto requestDto = new ShortenUrlRequestDto();

    ShortenUrlResponseDto responseDto =
        new ShortenUrlResponseDto("abc123", longUrl, ttl, "http://url-shortener/abc123");

    requestDto.setLongUrl(longUrl);

    when(urlShortenerService.createShortUrl(requestDto, requestedTtl)).thenReturn(responseDto);

    mockMvc
        .perform(
            post("/api/v1/url-shortener")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("Short URL created successfully"))
        .andExpect(jsonPath("$.statusCode").value(201));
  }

  @Test
  void testWhenInvalidLongUrlProvided() throws Exception {

    ShortenUrlRequestDto requestDto = new ShortenUrlRequestDto();
    requestDto.setLongUrl("invalid-url");

    mockMvc
        .perform(
            post("/api/v1/url-shortener")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.statusCode").value(400))
        .andExpect(jsonPath("$.data.longUrl").value("Invalid URL format"));
  }

  @Test
  void testDeleteShortUrl() throws Exception {
    String id = "abc123";

    mockMvc
        .perform(delete("/api/v1/url-shortener/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Shorten url deleted successfully"))
        .andExpect(jsonPath("$.statusCode").value(200));
  }
}
