package com.itimpulse.urlshortener.service;

import com.itimpulse.urlshortener.dto.ShortenUrlRequestDto;
import com.itimpulse.urlshortener.dto.ShortenUrlResponseDto;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlShortenerServiceImpl implements UrlShortenerService {
    private final ShortIdGenerator shortIdGenerator;
    private final ShortenUrlRepository shortenUrlRepository;
    private final UrlBuilder urlBuilder;

    @Autowired
    public UrlShortenerServiceImpl(ShortenUrlRepository shortenUrlRepository,
                             ShortIdGenerator shortIdGenerator,
                             UrlBuilder urlBuilder) {
        this.shortenUrlRepository = shortenUrlRepository;
        this.shortIdGenerator = shortIdGenerator;
        this.urlBuilder = urlBuilder;
    }

    @Override
    public ShortenUrlResponseDto createShortUrl(ShortenUrlRequestDto requestDto, Integer ttl) {
        String shortId = (requestDto.getCustomId() != null && !requestDto.getCustomId().isEmpty())
                ? requestDto.getCustomId()
                : shortIdGenerator.generate();

        if (shortenUrlRepository.existsById(shortId)) {
            log.warn("Short ID '{}' already exists", shortId);
            throw new ConflictException("The provided ID already exists. Please choose a different ID.");
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

    @Override
    public void deleteShortUrl(String id) {

        shortenUrlRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The provided ID could not be found."));

        shortenUrlRepository.deleteById(id);
        log.info("Deleted short URL with ID: {}", id);
    }

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
