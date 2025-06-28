package com.itimpulse.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UrlShortenerOnboardingApplication {

  public static void main(String[] args) {
    SpringApplication.run(UrlShortenerOnboardingApplication.class, args);
  }
}
