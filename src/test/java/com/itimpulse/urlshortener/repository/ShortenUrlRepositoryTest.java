package com.itimpulse.urlshortener.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.itimpulse.urlshortener.model.ShortenUrl;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class ShortenUrlRepositoryTest {

  @Autowired private ShortenUrlRepository repository;

  @Test
  void testFindByTtlBefore() {
    LocalDateTime now = LocalDateTime.now();

    ShortenUrl expiredUrl = new ShortenUrl();
    expiredUrl.setId("abc123");
    expiredUrl.setUrl("https://expired.com");
    expiredUrl.setTtl(now.minusDays(1));
    repository.save(expiredUrl);

    ShortenUrl validUrl = new ShortenUrl();
    validUrl.setId("xyz789");
    validUrl.setUrl("https://valid.com");
    validUrl.setTtl(now.plusDays(1));
    repository.save(validUrl);

    // When
    List<ShortenUrl> result = repository.findByTtlBefore(now);

    // Then
    assertThat(result).hasSize(1).extracting(ShortenUrl::getId).containsExactly("abc123");
  }
}
