package com.hikmethankolay.template.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.hikmethankolay.template.IntegrationTest;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
        for (int i = 0; i < 3; i++) {
            assertThat(service.tryAcquire(key)).isTrue();
        }

        assertThat(service.tryAcquire(key)).isFalse();
    }

    @Test
    void blocksTheAttemptIfAnyKeyIsBlocked() {
        String ip = uniqueKey("ip");
        String account = uniqueKey("account");
        for (int i = 0; i < 3; i++) {
            service.tryAcquire(ip, account);
        }

        assertThat(service.tryAcquire(ip, uniqueKey("account"))).isFalse();
        assertThat(service.tryAcquire(uniqueKey("ip"), account)).isFalse();
        assertThat(service.tryAcquire(uniqueKey("ip"), uniqueKey("account"))).isTrue();
    }

    @Test
    void resetUnblocksKey() {
        String key = uniqueKey("account");
        for (int i = 0; i < 3; i++) {
            service.tryAcquire(key);
        }

        service.reset(key);

        assertThat(service.tryAcquire(key)).isTrue();
    }

    @Test
    void releaseTakesBackOneAttempt() {
        String key = uniqueKey("ip");
        for (int i = 0; i < 3; i++) {
            service.tryAcquire(key);
        }

        service.release(key);

        assertThat(service.tryAcquire(key)).isTrue();
        assertThat(service.tryAcquire(key)).isFalse();
    }

    @Test
    void concurrentAttemptsCannotExceedTheLimit() throws Exception {
        String key = uniqueKey("account");
        try (ExecutorService pool = Executors.newFixedThreadPool(20)) {
            List<Future<Boolean>> results =
                    pool.invokeAll(Collections.nCopies(20, (Callable<Boolean>) () -> service.tryAcquire(key)));
            long allowed = 0;
            for (Future<Boolean> result : results) {
                allowed += result.get() ? 1 : 0;
            }
            assertThat(allowed).isEqualTo(3);
        }
    }

    @Test
    void countersExpireAfterLockDuration() {
        String key = uniqueKey("ip");
        service.tryAcquire(key);

        Long ttlSeconds = redis.getExpire("spring-boot-template:login-failures:" + key);
        assertThat(ttlSeconds)
                .isPositive()
                .isLessThanOrEqualTo(service.lockDuration().toSeconds());
    }
}
