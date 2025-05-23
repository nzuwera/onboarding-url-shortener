package com.itimpulse.urlshortener.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortIdGenerator {
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String ALPHANUMERIC = LETTERS + DIGITS;
    private static final int SHORT_ID_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder generatedId = new StringBuilder(SHORT_ID_LENGTH);
        generatedId.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        generatedId.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        for (int i = 2; i < SHORT_ID_LENGTH; i++) {
            generatedId.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return shuffleString(generatedId.toString());
    }

    private String shuffleString(String input) {
        char[] array = input.toCharArray();
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = array[i];
            array[i] = array[index];
            array[index] = temp;
        }
        return new String(array);
    }
}
