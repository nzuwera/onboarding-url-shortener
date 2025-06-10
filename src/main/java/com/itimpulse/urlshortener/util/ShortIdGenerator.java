package com.itimpulse.urlshortener.util;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

/**
 * Utility component for generating secure, random short IDs for URLs.
 *
 * <p>This generator creates 6-character alphanumeric IDs with the following characteristics: -
 * Always contains at least one letter and one digit - Uses cryptographically secure randomization -
 * Characters are shuffled to ensure unpredictability - Case-sensitive (includes both uppercase and
 * lowercase letters)
 *
 * <p>The algorithm ensures a good balance between: - Security: Uses SecureRandom for cryptographic
 * strength - Uniqueness: Probability of collision is very low
 */
@Component
public class ShortIdGenerator {
  /** Character set containing all lowercase and uppercase letters */
  private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /** Character set containing all digits 0-9 */
  private static final String DIGITS = "0123456789";

  /** Combined character set for general random selection */
  private static final String ALPHANUMERIC = LETTERS + DIGITS;

  /** Fixed length for generated short IDs */
  private static final int SHORT_ID_LENGTH = 6;

  /** Cryptographically secure random number generator */
  private final SecureRandom random = new SecureRandom();

  /**
   * Generates a secure, random 6-character short ID.
   *
   * <p>The generation algorithm ensures each ID contains: 1. At least one letter 2. At least one
   * digit 3. Four additional random alphanumeric characters 4. All characters are then shuffled for
   * unpredictability
   *
   * <p>This approach guarantees that every generated ID meets the validation requirements while
   * maintaining security.
   *
   * <p>Example outputs: "a7Xm9K", "B2nQ8p", "9zA4Lm"
   *
   * @return String containing a 6-character alphanumeric ID
   */
  public String generate() {
    StringBuilder generatedId = new StringBuilder(SHORT_ID_LENGTH);
    generatedId.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
    generatedId.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
    for (int i = 2; i < SHORT_ID_LENGTH; i++) {
      generatedId.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
    }
    return shuffleString(generatedId.toString());
  }

  /**
   * Shuffles the characters in a string
   *
   * <p>This method randomly rearranges all characters in the input string, ensuring that the
   * guaranteed letter and digit from the generation process are not always in predictable
   * positions.
   *
   * @param input The string to shuffle
   * @return String with characters randomly rearranged
   */
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
