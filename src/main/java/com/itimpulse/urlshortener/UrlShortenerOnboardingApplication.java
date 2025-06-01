package com.itimpulse.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class UrlShortenerOnboardingApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerOnboardingApplication.class, args);
    }

}
