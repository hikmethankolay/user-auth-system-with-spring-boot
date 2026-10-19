package com.hikmethankolay.template.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.hikmethankolay.template.IntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Login lockout counters in Redis (max attempts is 3 in the test profile). */
class LoginAttemptServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private LoginAttemptService service;

    @Autowired
    private StringRedisTemplate redis;

    private static String uniqueKey(String type) {
        return type + ":" + UUID.randomUUID();
    }

    @Test
    void blocksKeyAfterMaxAttempts() {
        String key = uniqueKey("ip");
        service.recordFailure(key);
        service.recordFailure(key);
        assertThat(service.isBlocked(key)).isFalse();

        service.recordFailure(key);
        assertThat(service.isBlocked(key)).isTrue();
    }

    @Test
    void recordsEveryGivenKey() {
        String ip = uniqueKey("ip");
        String account = uniqueKey("account");
        for (int i = 0; i < 3; i++) {
            service.recordFailure(ip, account);
        }

        assertThat(service.isBlocked(ip)).isTrue();
        assertThat(service.isBlocked(account)).isTrue();
        assertThat(service.isBlocked(uniqueKey("account"))).isFalse();
    }

    @Test
    void resetUnblocksKey() {
        String key = uniqueKey("account");
        for (int i = 0; i < 3; i++) {
            service.recordFailure(key);
        }

        service.reset(key);

        assertThat(service.isBlocked(key)).isFalse();
    }

    @Test
    void countersExpireAfterLockDuration() {
        String key = uniqueKey("ip");
        service.recordFailure(key);

        Long ttlSeconds = redis.getExpire("spring-boot-template:login-failures:" + key);
        assertThat(ttlSeconds).isPositive().isLessThanOrEqualTo(service.lockDuration().toSeconds());
    }
}
