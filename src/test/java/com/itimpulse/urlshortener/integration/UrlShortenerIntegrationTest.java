package com.itimpulse.urlshortener.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itimpulse.urlshortener.dto.ShortenUrlRequestDto;
import com.itimpulse.urlshortener.model.ShortenUrl;
import com.itimpulse.urlshortener.repository.ShortenUrlRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UrlShortenerIntegrationTest {

@SuppressWarnings("resource")
@Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

@SuppressWarnings("resource")
@Container
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShortenUrlRepository shortenUrlRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @BeforeEach
    void setUp() {
        shortenUrlRepository.deleteAll();
    }

    @Test
    void testCreateShortUrlWithGeneratedId() throws Exception {
        ShortenUrlRequestDto requestDto = new ShortenUrlRequestDto();
        requestDto.setLongUrl("https://example.com");

        mockMvc.perform(post("/api/v1/url-shortener")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Short URL created successfully"))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.url").value("https://example.com"))
                .andExpect(jsonPath("$.data.shortenUrl").exists());
    }

    @Test
    void testCreateShortUrlWithCustomId() throws Exception {
        ShortenUrlRequestDto requestDto = new ShortenUrlRequestDto();
        requestDto.setLongUrl("https://example.com");
        requestDto.setCustomId("custom123");

        mockMvc.perform(post("/api/v1/url-shortener")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Short URL created successfully"))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.id").value("custom123"))
                .andExpect(jsonPath("$.data.url").value("https://example.com"));
    }

    @Test
    void testRedirectToOriginalUrl() throws Exception {
        // Create a short URL using the API first
        ShortenUrlRequestDto requestDto = new ShortenUrlRequestDto();
        requestDto.setLongUrl("https://example.com");
        requestDto.setCustomId("test123");

        mockMvc.perform(post("/api/v1/url-shortener")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        // Now test the redirect
        mockMvc.perform(get("/test123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }

    @Test
    void testRedirectToNonExistentUrl() throws Exception {
        mockMvc.perform(get("/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The provided ID could not be found."))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    void testRedirectToExpiredUrl() throws Exception {
        // Manually create an expired URL in the database
        ShortenUrl shortUrl = new ShortenUrl();
        shortUrl.setId("expired123");
        shortUrl.setUrl("https://example.com");
        shortUrl.setTtl(LocalDateTime.now().minusHours(1)); // Already expired
        shortenUrlRepository.saveAndFlush(shortUrl);

        mockMvc.perform(get("/expired123"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.message").value("The requested short URL has expired and is no longer accessible."))
                .andExpect(jsonPath("$.statusCode").value(410));
    }

    @Test
    void testCreateShortUrlWithDuplicateCustomId() throws Exception {
        // Create the first short URL using the API
        ShortenUrlRequestDto firstRequestDto = new ShortenUrlRequestDto();
        firstRequestDto.setLongUrl("https://existing.com");
        firstRequestDto.setCustomId("duplicate123");

        mockMvc.perform(post("/api/v1/url-shortener")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequestDto)))
                .andExpect(status().isCreated());

        // Try to create another short URL with the same custom ID
        ShortenUrlRequestDto requestDto = new ShortenUrlRequestDto();
        requestDto.setLongUrl("https://example.com");
        requestDto.setCustomId("duplicate123");

        mockMvc.perform(post("/api/v1/url-shortener")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The provided ID already exists. Please choose a different ID."))
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    void testCreateShortUrlWithInvalidUrl() throws Exception {
        ShortenUrlRequestDto requestDto = new ShortenUrlRequestDto();
        requestDto.setLongUrl("invalid-url");

        mockMvc.perform(post("/api/v1/url-shortener")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void testDeleteShortUrl() throws Exception {
        // Create a short URL using the API first
        ShortenUrlRequestDto requestDto = new ShortenUrlRequestDto();
        requestDto.setLongUrl("https://example.com");
        requestDto.setCustomId("delete123");

        mockMvc.perform(post("/api/v1/url-shortener")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        // Now test the deletion
        mockMvc.perform(delete("/api/v1/url-shortener/delete123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Shorten url deleted successfully"))
                .andExpect(jsonPath("$.statusCode").value(200));

        // Verify the URL is deleted
        mockMvc.perform(get("/delete123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteNonExistentShortUrl() throws Exception {
        mockMvc.perform(delete("/api/v1/url-shortener/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("The provided ID could not be found."))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    @Test
    void testWelcomeEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/url-shortener/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to URL - Shortener Service!"));
    }
} 
