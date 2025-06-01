package com.itimpulse.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itimpulse.urlshortener.dto.ShortenUrlRequestDto;
import com.itimpulse.urlshortener.dto.ShortenUrlResponseDto;
import com.itimpulse.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UrlShortenerController.class)
class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlShortenerService urlShortenerService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateShortenUrl() throws Exception {
        int requestedTtl = 1;
        LocalDateTime ttl = LocalDateTime.now().minusSeconds(requestedTtl);
        String longUrl = "http://long-url.com";

        ShortenUrlRequestDto requestDto = new ShortenUrlRequestDto();

        ShortenUrlResponseDto responseDto = new ShortenUrlResponseDto("abc123", longUrl, ttl, "http://url-shortener/abc123");

        requestDto.setLongUrl(longUrl);

        when(urlShortenerService.createShortUrl(requestDto, requestedTtl)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/shorten-url")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Short URL created successfully"))
                .andExpect(jsonPath("$.statusCode").value(201));
    }

    @Test
    void testShortenUrl() throws Exception {
        int requestedTtl = 1;
        LocalDateTime ttl = LocalDateTime.now().minusSeconds(requestedTtl);
        String longUrl = "http://long-url.com";

        ShortenUrlRequestDto requestDto = new ShortenUrlRequestDto();

        ShortenUrlResponseDto responseDto = new ShortenUrlResponseDto("abc123", longUrl, ttl, "http://url-shortener/abc123");

        requestDto.setLongUrl(longUrl);

        when(urlShortenerService.createShortUrl(requestDto, requestedTtl)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/shorten-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Short URL created successfully"))
                .andExpect(jsonPath("$.statusCode").value(201));
    }

    @Test
    void deleteShortUrl() {

        String id = "abc123";

//        verify(urlShortenerService.deleteShortUrl(id)).
    }

}