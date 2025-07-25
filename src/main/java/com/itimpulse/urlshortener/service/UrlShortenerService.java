package com.itimpulse.urlshortener.service;

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
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service implementation for URL shortening operations.
 *
 * <p>This service provides the core business logic for: - Creating shortened URLs with validation
 * and conflict detection - Retrieving URLs with expiration checking - Deleting individual URLs -
 * Automatically cleaning up expired URLs via scheduled tasks
 *
 * <p>The service integrates with: - ShortenUrlRepository for data persistence - ShortIdGenerator
 * for creating unique identifiers - UrlBuilder for constructing complete shortened URLs
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UrlShortenerService implements IUrlShortenerService {

  private final ShortIdGenerator shortIdGenerator;
  private final ShortenUrlRepository shortenUrlRepository;
  private final UrlBuilder urlBuilder;
  private final RedisTemplate<String, Object> redisTemplate;

  private static final long DEFAULT_CACHE_TTL_HOURS = 24;

  /**
   * Validates if a custom ID is alphanumeric
   *
   * @param customId The custom ID to validate
   * @return true if valid, false otherwise
   */
  private boolean isValidCustomId(String customId) {
    return customId != null && customId.matches("^[a-zA-Z0-9]+$");
  }

  /**
   * Creates a shortened URL from the provided request data.
   *
   * <p>This method implements the core URL shortening logic: 1. Determines the short ID (custom or
   * generated) 2. Checks for ID conflicts in the database 3. Creates and persists the URL entity 4.
   * Builds and returns the complete response
   *
   * <p>The TTL (Time-To-Live) is optional and, when provided, sets an expiration time for the
   * shortened URL. After expiration, the URL becomes inaccessible.
   *
   * @param requestDto Contains the long URL and optional custom ID
   * @param ttl Optional time-to-live in hours for URL expiration
   * @return ShortenUrlResponseDto containing the shortened URL details
   * @throws ConflictException if the provided custom ID already exists
   */
  @Override
  public ShortenUrlResponseDto createShortUrl(ShortenUrlRequestDto requestDto, Integer ttl) {
    String shortId;
    
    // Use custom ID from the request if provided and valid, otherwise generate a new one.
    if (requestDto.getCustomId() != null && !requestDto.getCustomId().isEmpty()) {
      if (!isValidCustomId(requestDto.getCustomId())) {
        throw new BadRequestException("Invalid custom id");
      }
      shortId = requestDto.getCustomId();
    } else {
      shortId = shortIdGenerator.generate();
    }

    // Check if the generated or custom ID already exists in the database to prevent conflicts.
    if (shortenUrlRepository.existsById(shortId)) {
      log.warn("Short ID '{}' already exists", shortId);
      throw new ConflictException("The provided ID already exists. Please choose a different ID.");
    }

    ShortenUrl shortUrl = new ShortenUrl();
    shortUrl.setId(shortId);
    shortUrl.setUrl(requestDto.getLongUrl());
    // Set the expiration time if a TTL is provided.
    shortUrl.setTtl(ttl != null ? LocalDateTime.now().plusHours(ttl) : null);

    shortenUrlRepository.save(shortUrl);

    // Cache the newly created URL in Redis to speed up future lookups.
    // Use the provided TTL for the cache entry, or a default value if none is set.
    String cacheKey = "url:" + shortId;
    if (ttl != null) {
      redisTemplate.opsForValue().set(cacheKey, shortUrl, ttl.longValue(), TimeUnit.HOURS);
    } else {
      redisTemplate.opsForValue().set(cacheKey, shortUrl, DEFAULT_CACHE_TTL_HOURS, TimeUnit.HOURS);
    }

    ShortenUrlResponseDto responseDto = new ShortenUrlResponseDto();
    BeanUtils.copyProperties(shortUrl, responseDto);
    // Build the full short URL to return in the response.
    responseDto.setShortenUrl(urlBuilder.buildShortUrl(shortId));

    log.info("Created short URL: {}", responseDto.getShortenUrl());

    return responseDto;
  }

  /**
   * Retrieves a shortened URL by its ID with expiration validation.
   *
   * <p>This method performs critical validation: 1. Checks if the ID exists in the database 2.
   * Validates that the URL hasn't expired (if TTL was set) 3. Returns the URL entity for
   * redirection
   *
   * <p>The expiration check ensures that expired URLs are not accessible, maintaining data
   * integrity and preventing access to stale links.
   *
   * @param id The shortened URL identifier
   * @return ShortenUrl entity containing the original URL and metadata
   * @throws NotFoundException if the ID doesn't exist in the database
   * @throws UrlExpiredException if the URL has expired based on its TTL
   */
  @Override
  public ShortenUrl getShortUrl(String id) {
    String cacheKey = "url:" + id;

    // First, try to retrieve the URL from the Redis cache.
    ShortenUrl cachedUrl = (ShortenUrl) redisTemplate.opsForValue().get(cacheKey);
    if (cachedUrl != null) {
      // If found in cache, check if it has expired.
      if (cachedUrl.getTtl() != null && cachedUrl.getTtl().isBefore(LocalDateTime.now())) {
        // If expired, remove it from the cache and throw an exception.
        redisTemplate.delete(cacheKey);
        log.warn("Short URL '{}' expired", id);
        throw new UrlExpiredException(
            "The requested short URL has expired and is no longer accessible.");
      }
      // If not expired, return the cached URL.
      return cachedUrl;
    }

    // If the URL is not in the cache, retrieve it from the database.
    ShortenUrl url =
        shortenUrlRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("The provided ID could not be found."));

    // Check if the URL from the database has expired.
    if (url.getTtl() != null && url.getTtl().isBefore(LocalDateTime.now())) {

      log.warn("Short URL '{}' expired", id);
      throw new UrlExpiredException(
          "The requested short URL has expired and is no longer accessible.");
    }

    // Store the retrieved URL in the cache for future requests.
    redisTemplate.opsForValue().set(cacheKey, url, DEFAULT_CACHE_TTL_HOURS, TimeUnit.HOURS);

    return url;
  }

  /**
   * Deletes a shortened URL by its ID.
   *
   * <p>This method performs a safe deletion: 1. Verifies the ID exists before attempting deletion
   * 2. Removes the URL from the database 3. Logs the deletion for audit purposes
   *
   * <p>After deletion, the shortened URL becomes permanently inaccessible.
   *
   * @param id The shortened URL identifier to delete
   * @throws NotFoundException if the ID doesn't exist in the database
   */
  @Override
  public void deleteShortUrl(String id) {
    shortenUrlRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("The provided ID could not be found."));

    shortenUrlRepository.deleteById(id);

    // Also remove the URL from the Redis cache upon deletion.
    redisTemplate.delete("url:" + id);
    log.info("Deleted short URL with ID: {}", id);
  }

  /**
   * Scheduled task to automatically clean up expired URLs.
   *
   * <p>This method runs hourly (at minute 0 of every hour) to maintain database hygiene by removing
   * expired URLs. This prevents: - Populating the database with expired entries - Potential
   * conflicts with expired IDs - Unnecessary storage usage
   *
   * <p>The cleanup process: 1. Finds all URLs with TTL before the current time 2. Iterates through
   * expired URLs and deletes them 3. Logs each deletion for monitoring purposes
   *
   * <p>Cron expression "0 0 * * * ?" means: - Second: 0 - Minute: 0 - Hour: * (every hour) - Day of
   * month: * (every day) - Month: * (every month) - Day of week: ? (any day)
   */
  @Scheduled(cron = "0 0 * * * ?") // Every hour at minute 0
  public void deleteExpiredShortUrls() {
    LocalDateTime now = LocalDateTime.now();

    List<ShortenUrl> expiredUrls = shortenUrlRepository.findByTtlBefore(now);

    // Iterate over the list of expired URLs and delete them from the database and cache.
    for (ShortenUrl url : expiredUrls) {
      shortenUrlRepository.delete(url);
      redisTemplate.delete("url:" + url.getId());
      log.info("Deleted expired URL with ID: {}", url.getId());
    }
  }
}
