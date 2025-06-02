package com.itimpulse.urlshortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlBuilderTest {

    @Test
    void testBuildShortUrl() {
        UrlBuilder urlBuilder = new UrlBuilder();
        urlBuilder.setAllowedOrigin("http://short.ly");

        String result = urlBuilder.buildShortUrl("abc123");

        assertEquals("http://short.ly/abc123", result);
    }
}