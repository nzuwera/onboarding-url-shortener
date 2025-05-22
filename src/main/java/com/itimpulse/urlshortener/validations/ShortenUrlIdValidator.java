package com.itimpulse.urlshortener.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ShortenUrlIdValidator implements ConstraintValidator<ValidShortenUrlId, String> {

    @Getter
    private static class ValidationRule {
        private final Predicate<String> predicate;
        private final String errorMessage;

        public ValidationRule(Predicate<String> predicate, String errorMessage) {
            this.predicate = predicate;
            this.errorMessage = errorMessage;
        }

        public boolean isValid(String value) {
            return predicate.test(value);
        }
    }

    private final List<ValidationRule> rules = new ArrayList<>();

    @Override
    public void initialize(ValidShortenUrlId constraintAnnotation) {
        rules.add(new ValidationRule(
                customId -> customId == null || customId.isEmpty() || customId.length() >= 6,
                "customId must be at least 6 characters long."
        ));
        rules.add(new ValidationRule(
                customId -> customId == null || customId.isEmpty() || customId.matches(".*[a-zA-Z].*"),
                "customId must contain letters."
        ));
        rules.add(new ValidationRule(
                customId -> customId == null || customId.isEmpty() || customId.matches(".*\\d.*"),
                "customId must contain at least one digit."
        ));
        rules.add(new ValidationRule(
                customId -> customId == null || customId.isEmpty() || !customId.matches(".*\\s.*"),
                "customId cannot contain whitespace."
        ));
    }

    @Override
    public boolean isValid(String customId, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        for (ValidationRule rule : rules) {
            if (!rule.isValid(customId)) {
                context.buildConstraintViolationWithTemplate(rule.getErrorMessage())
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
