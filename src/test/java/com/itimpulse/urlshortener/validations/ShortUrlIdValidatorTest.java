package com.itimpulse.urlshortener.validations;

import static org.junit.jupiter.api.Assertions.*;

import com.itimpulse.urlshortener.dto.ShortenUrlRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ShortUrlIdValidatorTest {
  private static ValidatorFactory validatorFactory;
  private static Validator validator;

  @BeforeAll
  static void setUp() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  @AfterAll
  static void tearDown() {
    validatorFactory.close();
  }

  @Test
  void validCustomId_passesAllRules() {
    ShortenUrlRequestDto dto = new ShortenUrlRequestDto();
    dto.setLongUrl("https://example.com");
    dto.setCustomId("abc123");
    Set<ConstraintViolation<ShortenUrlRequestDto>> violations = validator.validate(dto);
    assertTrue(
        violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("customId")));
  }
}
