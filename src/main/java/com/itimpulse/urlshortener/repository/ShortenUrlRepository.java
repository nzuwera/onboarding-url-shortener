package com.itimpulse.urlshortener.repository;

import com.itimpulse.urlshortener.model.ShortenUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShortenUrlRepository extends JpaRepository<ShortenUrl, String> {
    List<ShortenUrl> findByTtlBefore(LocalDateTime ttl);
}
