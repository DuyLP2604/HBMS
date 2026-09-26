package util.security;

import java.security.SecureRandom;

public final class OtpGenerator {

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private OtpGenerator() {
    }

    public static String generate() {

        int value =
                RANDOM.nextInt(1_000_000);

        return String.format(
                "%06d",
                value
        );
    }
}