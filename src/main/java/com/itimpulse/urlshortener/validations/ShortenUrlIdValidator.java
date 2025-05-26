package com.itimpulse.urlshortener.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Custom validator for shortened URL IDs (custom IDs).
 * 
 * This validator ensures that user-provided custom IDs meet security and
 * usability requirements. It implements a rule-based validation system
 * where each rule is evaluated independently and provides specific error messages.
 * 
 * Validation Rules:
 * 1. Minimum length of 6 characters
 * 2. Must contain at least one letter
 * 3. Must contain at least one digit
 * 4. Cannot contain whitespace
 * 
 * The validator allows null or empty values (optional field validation),
 * but enforces rules when a value is provided.
 * 
 */
public class ShortenUrlIdValidator implements ConstraintValidator<ValidShortenUrlId, String> {

    /**
     * Inner class representing a single validation rule.
     * 
     * Each rule consists of:
     * - A predicate function that tests the validity condition
     * - A specific error message for when the rule fails
     * 
     * This design allows for:
     * - Clear separation of validation logic
     * - Specific error messages for each validation failure
     * - Easy addition or modification of rules
     * - Functional programming approach for clean code
     */
    @Getter
    private static class ValidationRule {
        /** Function that tests whether a string meets the validation criteria */
        private final Predicate<String> predicate;
        
        /** Error message to display when validation fails */
        private final String errorMessage;

        /**
         * Creates a new validation rule.
         * 
         * @param predicate Function that tests whether a string meets the validation criteria
         * @param errorMessage Message to display when validation fails
         */
        public ValidationRule(Predicate<String> predicate, String errorMessage) {
            this.predicate = predicate;
            this.errorMessage = errorMessage;
        }

        /**
         * Tests if the provided value satisfies this validation rule.
         * 
         * @param value The string value to validate
         * @return true if the value passes this rule, false otherwise
         */
        public boolean isValid(String value) {
            return predicate.test(value);
        }
    }

    /** List of all validation rules to be applied */
    private final List<ValidationRule> rules = new ArrayList<>();

    /**
     * Initializes the validator with all validation rules.
     * 
     * This method is called once when the validator is created and sets up
     * all the validation rules with their corresponding regex patterns and
     * error messages.
     * 
     * The rules are designed to allow null/empty values (since customId is optional)
     * but enforce requirements when a value is provided.
     * 
     * @param constraintAnnotation
     */
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

    /**
     * Validates the provided custom ID against all defined rules.
     * 
     * This method checks if the custom ID meets all validation criteria.
     * If any rule fails, it adds a specific constraint violation message
     * to the context and returns false.
     * 
     * @param customId The custom ID to validate
     * @param context The validation context for adding constraint violations
     * @return true if the custom ID is valid, false otherwise
     */
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
