package com.itimpulse.urlshortener.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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

        Pattern containsLetter = Pattern.compile(".*[a-zA-Z].*");
        Pattern containsDigit = Pattern.compile(".*\\d.*");
        Pattern noWhitespace = Pattern.compile("^\\S*$");

        rules.add(new ValidationRule(
                customId -> customId == null || customId.isEmpty() || customId.length() >= 6,
                "customId must be at least 6 characters long."
        ));
        rules.add(new ValidationRule(
                customId -> customId == null || customId.isEmpty() || containsLetter.matcher(customId).matches(),
                "customId must contain letters."
        ));
        rules.add(new ValidationRule(
                customId -> customId == null || customId.isEmpty() || containsDigit.matcher(customId).matches(),
                "customId must contain at least one digit."
        ));
        rules.add(new ValidationRule(
                customId -> customId == null || customId.isEmpty() || noWhitespace.matcher(customId).matches(),
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
