package com.hikmethankolay.template.mfa;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.OptionalLong;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Time-based one-time passwords (RFC 6238) with the parameters every authenticator app supports: HMAC-SHA1,
 * 6 digits, 30-second steps.
 */
public final class Totp {

    /** Length of a time step in seconds. */
    static final long STEP_SECONDS = 30;

    /** Steps accepted before and after the current one, to tolerate clock drift and typing time. */
    private static final int WINDOW = 1;

    private static final int DIGITS = 6;

    private static final int SECRET_BYTES = 20;

    private static final char[] BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    private static final SecureRandom RANDOM = new SecureRandom();

    private Totp() {}

    /**
     * Creates a random 160-bit secret, as recommended by RFC 4226.
     *
     * @return the secret
     */
    public static byte[] generateSecret() {
        byte[] secret = new byte[SECRET_BYTES];
        RANDOM.nextBytes(secret);
        return secret;
    }

    /**
     * Returns the time step of an instant.
     *
     * @param epochSecond seconds since the epoch
     * @return the 30-second step
     */
    public static long step(long epochSecond) {
        return Math.floorDiv(epochSecond, STEP_SECONDS);
    }

    /**
     * Computes the code for a time step.
     *
     * @param secret the shared secret
     * @param step   the time step
     * @return the 6-digit code, zero-padded
     */
    public static String code(byte[] secret, long step) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] hash =
                    mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(step).array());
            // Dynamic truncation, RFC 4226 section 5.3.
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            return String.format("%06d", binary % 1_000_000);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("HmacSHA1 is not available", ex);
        }
    }

    /**
     * Finds the time step a code belongs to, within one step of {@code epochSecond}.
     *
     * @param secret      the shared secret
     * @param code        the code the user typed
     * @param epochSecond the current time in seconds since the epoch
     * @param afterStep   the last step already used; it and earlier steps are refused, so a code works once
     * @return the matching step, or empty if the code is wrong, expired or already used
     */
    public static OptionalLong verify(byte[] secret, String code, long epochSecond, long afterStep) {
        if (code == null || code.length() != DIGITS) {
            return OptionalLong.empty();
        }
        byte[] typed = code.getBytes(StandardCharsets.US_ASCII);
        long current = step(epochSecond);
        for (long candidate = current - WINDOW; candidate <= current + WINDOW; candidate++) {
            // Constant-time comparison: the time taken must not reveal how many digits were right.
            if (candidate > afterStep
                    && MessageDigest.isEqual(typed, code(secret, candidate).getBytes(StandardCharsets.US_ASCII))) {
                return OptionalLong.of(candidate);
            }
        }
        return OptionalLong.empty();
    }

    /**
     * Builds the {@code otpauth://} URI that authenticator apps read from a QR code.
     *
     * @param issuer  the application name shown in the app
     * @param account the user's name shown in the app
     * @param secret  the shared secret
     * @return the URI
     */
    public static String otpauthUri(String issuer, String account, byte[] secret) {
        String label = encode(issuer) + ":" + encode(account);
        return "otpauth://totp/" + label + "?secret=" + base32(secret) + "&issuer=" + encode(issuer)
                + "&algorithm=SHA1&digits=" + DIGITS + "&period=" + STEP_SECONDS;
    }

    /**
     * Encodes bytes as unpadded Base32 (RFC 4648), the format authenticator apps expect for secrets.
     *
     * @param bytes the bytes
     * @return the Base32 text
     */
    public static String base32(byte[] bytes) {
        StringBuilder out = new StringBuilder((bytes.length * 8 + 4) / 5);
        int buffer = 0;
        int bits = 0;
        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xff);
            bits += 8;
            while (bits >= 5) {
                out.append(BASE32[(buffer >> (bits - 5)) & 0x1f]);
                bits -= 5;
            }
        }
        if (bits > 0) {
            out.append(BASE32[(buffer << (5 - bits)) & 0x1f]);
        }
        return out.toString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
