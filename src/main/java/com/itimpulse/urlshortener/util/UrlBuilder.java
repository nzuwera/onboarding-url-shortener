package com.itimpulse.urlshortener.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlBuilder {
  @Value("${spring.allowed-origin}")
  private String allowedOrigin;

  public void setAllowedOrigin(String allowedOrigin) {
    this.allowedOrigin = allowedOrigin;
  }

  public String buildShortUrl(String shortId) {
    return allowedOrigin + "/" + shortId;
  }
}
