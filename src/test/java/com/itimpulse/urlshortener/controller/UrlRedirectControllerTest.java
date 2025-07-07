package com.itimpulse.urlshortener.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.itimpulse.urlshortener.config.JpaAuditingConfig;
import com.itimpulse.urlshortener.exceptions.NotFoundException;
import com.itimpulse.urlshortener.exceptions.UrlExpiredException;
import com.itimpulse.urlshortener.model.ShortenUrl;
import com.itimpulse.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = UrlRedirectController.class,
    excludeAutoConfiguration = {JpaAuditingConfig.class})
class UrlRedirectControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UrlShortenerService urlShortenerService;


  @Test
  void testRedirectToOriginalUrl() throws Exception {
    String id = "abc123";
    String originalUrl = "http://long-url.com";

    ShortenUrl mockEntity = new ShortenUrl();
    mockEntity.setId(id);
    mockEntity.setUrl(originalUrl);

    when(urlShortenerService.getShortUrl(id)).thenReturn(mockEntity);

    mockMvc
        .perform(get("/" + id))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", originalUrl));
  }

  @Test
  void testWhenShortUrlIdNotFound() throws Exception {
    String nonExistentId = "abc123";

    when(urlShortenerService.getShortUrl(nonExistentId))
        .thenThrow(new NotFoundException("The provided ID could not be found."));

    mockMvc
        .perform(get("/" + nonExistentId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("The provided ID could not be found."))
        .andExpect(jsonPath("$.statusCode").value(404));
  }

  @Test
  void testWhenShortUrlIsExpired() throws Exception {
    String expiredId = "expired123";

    when(urlShortenerService.getShortUrl(expiredId))
        .thenThrow(
            new UrlExpiredException(
                "The requested short URL has expired and is no longer accessible."));

    mockMvc
        .perform(get("/" + expiredId))
        .andExpect(status().isGone())
        .andExpect(
            jsonPath("$.message")
                .value("The requested short URL has expired and is no longer accessible."))
        .andExpect(jsonPath("$.statusCode").value(410));
  }

}
