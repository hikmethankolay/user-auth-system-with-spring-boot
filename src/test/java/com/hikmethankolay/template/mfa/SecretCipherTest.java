package com.hikmethankolay.template.mfa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hikmethankolay.template.TestProperties;
import com.hikmethankolay.template.config.AppProperties;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class SecretCipherTest {

    private final SecretCipher cipher = new SecretCipher(TestProperties.create());

    @Test
    void decryptsWhatItEncrypted() {
        byte[] secret = Totp.generateSecret();

        String encrypted = cipher.encrypt(secret, 42);

        assertThat(cipher.decrypt(encrypted, 42)).isEqualTo(secret);
        // A random IV per value: the same secret never encrypts to the same text.
        assertThat(cipher.encrypt(secret, 42)).isNotEqualTo(encrypted);
    }

    @Test
    void aSecretCopiedToAnotherUserDoesNotDecrypt() {
        String encrypted = cipher.encrypt(Totp.generateSecret(), 42);

        assertThatThrownBy(() -> cipher.decrypt(encrypted, 43)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anotherKeyCannotDecrypt() {
        AppProperties p = TestProperties.create();
        SecretCipher otherKey = new SecretCipher(new AppProperties(
                p.jwt(),
                p.cookie(),
                p.cors(),
                p.login(),
                p.rateLimit(),
                p.account(),
                p.mail(),
                p.admin(),
                new AppProperties.Mfa("another-key-that-is-at-least-32-characters", Duration.ofMinutes(5)),
                p.socialLogin()));
        String encrypted = cipher.encrypt(Totp.generateSecret(), 42);

        assertThatThrownBy(() -> otherKey.decrypt(encrypted, 42)).isInstanceOf(IllegalStateException.class);
    }
}
