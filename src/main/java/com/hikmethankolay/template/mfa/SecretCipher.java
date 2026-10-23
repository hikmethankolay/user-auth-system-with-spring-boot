package com.hikmethankolay.template.mfa;

import com.hikmethankolay.template.config.AppProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * Encrypts TOTP secrets with AES-256-GCM, so a copy of the database alone does not reveal them.
 *
 * <p>The key is the SHA-256 of {@code app.mfa.encryption-key}. Each value is
 * {@code base64(iv || ciphertext || tag)} with a random 96-bit IV, and the user id is authenticated
 * data: a secret copied to another user's row does not decrypt.</p>
 */
@Component
public class SecretCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int IV_BYTES = 12;

    private static final int TAG_BITS = 128;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKey key;

    /**
     * Creates the cipher.
     *
     * @param properties application properties holding the encryption key
     */
    public SecretCipher(AppProperties properties) {
        this.key = new SecretKeySpec(sha256(properties.mfa().encryptionKey()), "AES");
    }

    /**
     * Encrypts a secret.
     *
     * @param plaintext the secret
     * @param userId    the owner, bound to the ciphertext
     * @return the encrypted value, Base64
     */
    public String encrypt(byte[] plaintext, long userId) {
        try {
            byte[] iv = new byte[IV_BYTES];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            cipher.updateAAD(aad(userId));
            byte[] ciphertext = cipher.doFinal(plaintext);
            return Base64.getEncoder()
                    .encodeToString(ByteBuffer.allocate(iv.length + ciphertext.length)
                            .put(iv)
                            .put(ciphertext)
                            .array());
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Could not encrypt the TOTP secret", ex);
        }
    }

    /**
     * Decrypts a secret.
     *
     * @param encrypted the value returned by {@link #encrypt(byte[], long)}
     * @param userId    the owner
     * @return the secret
     * @throws IllegalStateException if the value was encrypted with another key or for another user
     */
    public byte[] decrypt(String encrypted, long userId) {
        try {
            byte[] bytes = Base64.getDecoder().decode(encrypted);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, bytes, 0, IV_BYTES));
            cipher.updateAAD(aad(userId));
            return cipher.doFinal(bytes, IV_BYTES, bytes.length - IV_BYTES);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException("Could not decrypt the TOTP secret; was MFA_ENCRYPTION_KEY changed?", ex);
        }
    }

    private static byte[] aad(long userId) {
        return ByteBuffer.allocate(Long.BYTES).putLong(userId).array();
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
