package com.itimpulse.urlshortener.repository;

import com.itimpulse.urlshortener.model.ShortenUrl;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortenUrlRepository extends JpaRepository<ShortenUrl, String> {
  List<ShortenUrl> findByTtlBefore(LocalDateTime ttl);
}
