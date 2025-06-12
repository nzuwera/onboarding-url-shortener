package com.itimpulse.urlshortener.exceptions;

public class UrlExpiredException extends RuntimeException {
  public UrlExpiredException(String message) {
    super(message);
  }
}
