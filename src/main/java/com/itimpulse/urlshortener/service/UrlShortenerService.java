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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service implementation for URL shortening operations.
 * <p>
 * This service provides the core business logic for:
 * - Creating shortened URLs with validation and conflict detection
 * - Retrieving URLs with expiration checking
 * - Deleting individual URLs
 * - Automatically cleaning up expired URLs via scheduled tasks
 * <p>
 * The service integrates with:
 * - ShortenUrlRepository for data persistence
 * - ShortIdGenerator for creating unique identifiers
 * - UrlBuilder for constructing complete shortened URLs
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UrlShortenerService implements IUrlShortenerService {
    private final ShortIdGenerator shortIdGenerator;
    private final ShortenUrlRepository shortenUrlRepository;
    private final UrlBuilder urlBuilder;

    /**
     * Constructor for dependency injection.
     *
     * @param shortenUrlRepository Repository for database operations
     * @param shortIdGenerator     Utility for generating random IDs
     * @param urlBuilder           Utility for building complete shortened URLs
     */
    @Autowired
    public UrlShortenerService(ShortenUrlRepository shortenUrlRepository,
                               ShortIdGenerator shortIdGenerator,
                               UrlBuilder urlBuilder) {
        this.shortenUrlRepository = shortenUrlRepository;
        this.shortIdGenerator = shortIdGenerator;
        this.urlBuilder = urlBuilder;
    }

    /**
     * Creates a shortened URL from the provided request data.
     * <p>
     * This method implements the core URL shortening logic:
     * 1. Determines the short ID (custom or generated)
     * 2. Checks for ID conflicts in the database
     * 3. Creates and persists the URL entity
     * 4. Builds and returns the complete response
     * <p>
     * The TTL (Time-To-Live) is optional and, when provided, sets an expiration
     * time for the shortened URL. After expiration, the URL becomes inaccessible.
     *
     * @param requestDto Contains the long URL and optional custom ID
     * @param ttl        Optional time-to-live in hours for URL expiration
     * @return ShortenUrlResponseDto containing the shortened URL details
     * @throws ConflictException if the provided custom ID already exists
     */
    @Override
    public ShortenUrlResponseDto createShortUrl(ShortenUrlRequestDto requestDto, Integer ttl) {
        String shortId = (requestDto.getCustomId() != null && !requestDto.getCustomId().isEmpty())
                ? requestDto.getCustomId()
                : shortIdGenerator.generate();

        if (shortenUrlRepository.existsById(shortId)) {
            log.warn("Short ID '{}' already exists", shortId);
            throw new ConflictException("The provided ID already exists. Please choose a different ID.");
        }

        if (!validateCustomId(shortId)){
            throw new BadRequestException("Invalid custom id");
        }

        ShortenUrl shortUrl = new ShortenUrl();
        shortUrl.setId(shortId);
        shortUrl.setUrl(requestDto.getLongUrl());
        shortUrl.setTtl(ttl != null ? LocalDateTime.now().plusHours(ttl) : null);

        shortenUrlRepository.save(shortUrl);

        ShortenUrlResponseDto responseDto = new ShortenUrlResponseDto();
        BeanUtils.copyProperties(shortUrl, responseDto);
        responseDto.setShortenUrl(urlBuilder.buildShortUrl(shortId));

        log.info("Created short URL: {}", responseDto.getShortenUrl());

        return responseDto;
    }

    private boolean validateCustomId(String customId) {

        Pattern validator = Pattern.compile(".*[a-zA-Z0-9]{6}.*");

        return validator.matcher(customId).matches();
    }

    /**
     * Retrieves a shortened URL by its ID with expiration validation.
     * <p>
     * This method performs critical validation:
     * 1. Checks if the ID exists in the database
     * 2. Validates that the URL hasn't expired (if TTL was set)
     * 3. Returns the URL entity for redirection
     * <p>
     * The expiration check ensures that expired URLs are not accessible,
     * maintaining data integrity and preventing access to stale links.
     *
     * @param id The shortened URL identifier
     * @return ShortenUrl entity containing the original URL and metadata
     * @throws NotFoundException   if the ID doesn't exist in the database
     * @throws UrlExpiredException if the URL has expired based on its TTL
     */
    @Override
    public ShortenUrl getShortUrl(String id) {

        ShortenUrl url = shortenUrlRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The provided ID could not be found."));

        if (url.getTtl() != null && url.getTtl().isBefore(LocalDateTime.now())) {

            log.warn("Short URL '{}' expired", id);
            throw new UrlExpiredException("The requested short URL has expired and is no longer accessible.");
        }

        return url;
    }

    /**
     * Deletes a shortened URL by its ID.
     * <p>
     * This method performs a safe deletion:
     * 1. Verifies the ID exists before attempting deletion
     * 2. Removes the URL from the database
     * 3. Logs the deletion for audit purposes
     * <p>
     * After deletion, the shortened URL becomes permanently inaccessible.
     *
     * @param id The shortened URL identifier to delete
     * @throws NotFoundException if the ID doesn't exist in the database
     */
    @Override
    public void deleteShortUrl(String id) {

        Optional<ShortenUrl> shortUrl = shortenUrlRepository.findById(id);

        if (shortUrl.isPresent()) {
            shortUrl.ifPresent(url -> {
                                shortenUrlRepository.deleteById(url.getId());
                                log.info("Deleted short URL with ID: {}", id);
                            }
                    );
        }else {
            throw new NotFoundException("The provided ID could not be found.");
        }



    }

    /**
     * Scheduled task to automatically clean up expired URLs.
     * <p>
     * This method runs hourly (at minute 0 of every hour) to maintain database
     * hygiene by removing expired URLs. This prevents:
     * - Populating the database with expired entries
     * - Potential conflicts with expired IDs
     * - Unnecessary storage usage
     * <p>
     * The cleanup process:
     * 1. Finds all URLs with TTL before the current time
     * 2. Iterates through expired URLs and deletes them
     * 3. Logs each deletion for monitoring purposes
     * <p>
     * Cron expression "0 0 * * * ?" means:
     * - Second: 0
     * - Minute: 0
     * - Hour: * (every hour)
     * - Day of month: * (every day)
     * - Month: * (every month)
     * - Day of week: ? (any day)
     */
    @Scheduled(cron = "0 0 * * * ?") // Every hour at minute 0
    public void deleteExpiredShortUrls() {
        LocalDateTime now = LocalDateTime.now();

        List<ShortenUrl> expiredUrls = shortenUrlRepository.findByTtlBefore(now);

        for (ShortenUrl url : expiredUrls) {
            shortenUrlRepository.delete(url);
            log.info("Deleted expired URL with ID: {}", url.getId());
        }
    }
}
