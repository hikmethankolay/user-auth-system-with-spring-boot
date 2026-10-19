package com.hikmethankolay.template.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hikmethankolay.template.IntegrationTest;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.rate-limit.requests-per-minute=3")
class RateLimitIntegrationTest extends IntegrationTest {

    /** A fresh IP per test, so counters from other tests never interfere. */
    private static String randomIp() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return "10.%d.%d.%d".formatted(random.nextInt(256), random.nextInt(256), random.nextInt(1, 255));
    }

    @Test
    void requestsOverTheLimitAreRejected() throws Exception {
        String ip = randomIp();
        for (int i = 3; i > 0; i--) {
            mockMvc.perform(get("/api/v1/users/me").with(fromIp(ip)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("RateLimit-Limit", "3"))
                    .andExpect(header().string("RateLimit-Remaining", Integer.toString(i - 1)));
        }

        mockMvc.perform(get("/api/v1/users/me").with(fromIp(ip)))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.status").value(429));

        // Other clients are not affected.
        mockMvc.perform(get("/api/v1/users/me").with(fromIp(randomIp()))).andExpect(status().isUnauthorized());
    }

    @Test
    void actuatorIsNotLimited() throws Exception {
        String ip = randomIp();
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/actuator/health").with(fromIp(ip))).andExpect(status().isOk());
        }
    }
}
