package com.hikmethankolay.template.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.hikmethankolay.template.TestProperties;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    private final LoginAttemptService service = new LoginAttemptService(TestProperties.create());

    @Test
    void blocksKeyAfterMaxAttempts() {
        service.recordFailure("ip:1");
        service.recordFailure("ip:1");
        assertThat(service.isBlocked("ip:1")).isFalse();

        service.recordFailure("ip:1");
        assertThat(service.isBlocked("ip:1")).isTrue();
    }

    @Test
    void recordsEveryGivenKey() {
        for (int i = 0; i < 3; i++) {
            service.recordFailure("ip:2", "account:bob");
        }

        assertThat(service.isBlocked("ip:2")).isTrue();
        assertThat(service.isBlocked("account:bob")).isTrue();
        assertThat(service.isBlocked("account:alice")).isFalse();
    }

    @Test
    void resetUnblocksKey() {
        for (int i = 0; i < 3; i++) {
            service.recordFailure("account:carol");
        }

        service.reset("account:carol");

        assertThat(service.isBlocked("account:carol")).isFalse();
    }
}
