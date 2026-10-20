package com.hikmethankolay.template.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Random opaque tokens (email links, refresh tokens) and the hashes they are stored as.
 *
 * <p>Tokens are 256-bit random values, so a fast hash is enough: they cannot be guessed or
 * brute-forced the way passwords can. Only the hash is stored, so a database leak does not
 * reveal usable tokens.</p>
 */
public final class SecureTokens {

    private static final SecureRandom RANDOM = new SecureRandom();

    /** Not instantiable. */
    private SecureTokens() {}

    /**
     * Generates a new token.
     *
     * @return 32 random bytes, Base64url-encoded without padding (43 characters)
     */
    public static String generate() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Hashes a token for storage and lookup.
     *
     * @param token the raw token
     * @return the hex SHA-256 of the token (64 characters)
     */
    public static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
