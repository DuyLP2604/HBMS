package util.security;

import java.security.SecureRandom;

/** Generates the code sent during password recovery. */
public final class OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private OtpGenerator() {
    }

    /** Generate a six-digit code for password recovery. */
    public static String generate() {
        int value = RANDOM.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}
