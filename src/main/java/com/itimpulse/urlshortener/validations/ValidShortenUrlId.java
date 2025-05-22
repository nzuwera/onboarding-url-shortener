package com.itimpulse.urlshortener.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ShortenUrlIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidShortenUrlId {
    String message() default "Invalid Custom ID.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
