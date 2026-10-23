package com.hikmethankolay.template.mfa;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class TotpTest {

    // RFC 6238 appendix B: the SHA-1 test secret and its 8-digit codes; 6-digit codes are their last 6 digits.
    private static final byte[] RFC_SECRET = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

    @Test
    void codesMatchTheRfcTestVectors() {
        assertThat(Totp.code(RFC_SECRET, Totp.step(59))).isEqualTo("287082");
        assertThat(Totp.code(RFC_SECRET, Totp.step(1111111109))).isEqualTo("081804");
        assertThat(Totp.code(RFC_SECRET, Totp.step(1234567890))).isEqualTo("005924");
        assertThat(Totp.code(RFC_SECRET, Totp.step(2000000000))).isEqualTo("279037");
    }

    @Test
    void verifyAcceptsOneStepOfDriftAndReturnsTheStep() {
        long now = 1234567890;
        long step = Totp.step(now);

        assertThat(Totp.verify(RFC_SECRET, Totp.code(RFC_SECRET, step - 1), now, 0))
                .hasValue(step - 1);
        assertThat(Totp.verify(RFC_SECRET, Totp.code(RFC_SECRET, step + 1), now, 0))
                .hasValue(step + 1);
        assertThat(Totp.verify(RFC_SECRET, Totp.code(RFC_SECRET, step - 2), now, 0))
                .isEmpty();
        assertThat(Totp.verify(RFC_SECRET, "12345", now, 0)).isEmpty();
        assertThat(Totp.verify(RFC_SECRET, null, now, 0)).isEmpty();
    }

    @Test
    void verifyRefusesAStepThatWasAlreadyUsed() {
        long now = 1234567890;
        String code = Totp.code(RFC_SECRET, Totp.step(now));

        assertThat(Totp.verify(RFC_SECRET, code, now, Totp.step(now))).isEmpty();
        assertThat(Totp.verify(RFC_SECRET, code, now, Totp.step(now) - 1)).hasValue(Totp.step(now));
    }

    @Test
    void base32MatchesRfc4648() {
        assertThat(Totp.base32("".getBytes(StandardCharsets.US_ASCII))).isEmpty();
        assertThat(Totp.base32("f".getBytes(StandardCharsets.US_ASCII))).isEqualTo("MY");
        assertThat(Totp.base32("foobar".getBytes(StandardCharsets.US_ASCII))).isEqualTo("MZXW6YTBOI");
        assertThat(Totp.base32(RFC_SECRET)).isEqualTo("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ");
    }

    @Test
    void otpauthUriHasTheParametersAuthenticatorAppsRead() {
        assertThat(Totp.otpauthUri("My App", "alice", RFC_SECRET))
                .isEqualTo("otpauth://totp/My%20App:alice?secret=GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"
                        + "&issuer=My%20App&algorithm=SHA1&digits=6&period=30");
    }
}
