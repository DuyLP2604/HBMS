package util.security;

import java.security.SecureRandom;

public final class TemporaryPasswordGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#%_-";

    private TemporaryPasswordGenerator() {
    }

    public static String generate() {
        StringBuilder value = new StringBuilder(20);
        for (int i = 0; i < 20; i++) {
            value.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return value.toString();
    }
}
