package com.allergen.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecurePasswordGenerator {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIAL = "!@#$%&*-_=+";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;

    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a random password with length {@code length}, containing at least one character
     * from each class (upper, lower, digit, special).
     */
    public String generateStrong(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("length must be >= 8");
        }
        StringBuilder sb = new StringBuilder(length);
        sb.append(pick(UPPER));
        sb.append(pick(LOWER));
        sb.append(pick(DIGITS));
        sb.append(pick(SPECIAL));
        for (int i = sb.length(); i < length; i++) {
            sb.append(pick(ALL));
        }
        // Fisher–Yates shuffle chars
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }

    private char pick(String charset) {
        return charset.charAt(random.nextInt(charset.length()));
    }
}
