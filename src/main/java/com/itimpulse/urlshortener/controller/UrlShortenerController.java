package com.itimpulse.urlshortener.controller;

import com.itimpulse.urlshortener.dto.ShortenUrlRequestDto;
import com.itimpulse.urlshortener.dto.ShortenUrlResponseDto;
import com.itimpulse.urlshortener.model.ShortenUrl;
import com.itimpulse.urlshortener.service.UrlShortenerService;
import com.itimpulse.urlshortener.util.CustomResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping
@Slf4j
public class UrlShortenerController {

    private final UrlShortenerService urlshortenerService;

    public UrlShortenerController(UrlShortenerService urlshortenerService) {
        this.urlshortenerService = urlshortenerService;
    }

    @GetMapping("/")
    public String index() {
        return "Welcome to URL - Shortener Service!";
    }

    @PostMapping("/api/v1/shorten-url")
    public ResponseEntity<CustomResponse<ShortenUrlResponseDto>> shortenUrl(@RequestParam(required = false) Integer ttl,
                                                                            @RequestBody @Valid ShortenUrlRequestDto longUrl) {

        ShortenUrlResponseDto shortUrl = urlshortenerService.createShortUrl(longUrl, ttl);

        return ResponseEntity.status(HttpStatus.CREATED.value()).body(
                CustomResponse.successResponse("Short URL created successfully",
                        HttpStatus.CREATED.value(), shortUrl));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> redirectToOriginal(@PathVariable String id) {
        ShortenUrl url = urlshortenerService.getShortUrl(id);

        log.info("Redirecting to: {}", url.getUrl());
        return ResponseEntity.status(302).location(URI.create(url.getUrl())).build();
    }

    @DeleteMapping("/api/v1/shorten-url/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteShortUrl(@PathVariable String id) {
        urlshortenerService.deleteShortUrl(id);
        return ResponseEntity.ok(CustomResponse.successResponse("Shorten url deleted successfully", HttpStatus.OK.value()));
    }
}

