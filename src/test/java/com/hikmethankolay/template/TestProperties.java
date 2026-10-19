package com.hikmethankolay.template;

import com.hikmethankolay.template.config.AppProperties;
import java.time.Duration;
import java.util.List;

/** {@link AppProperties} for unit tests that do not start a Spring context. */
public final class TestProperties {

    public static final String JWT_SECRET = "unit-test-secret-that-is-at-least-32-chars";
    public static final String ISSUER = "test-issuer";

    private TestProperties() {
    }

    public static AppProperties create() {
        return new AppProperties(
                new AppProperties.Jwt(JWT_SECRET, ISSUER, Duration.ofMinutes(15), Duration.ofDays(30)),
                new AppProperties.Cookie("auth_token", false, "Strict"),
                new AppProperties.Cors(List.of("http://localhost:3000")),
                new AppProperties.Login(3, Duration.ofHours(1)),
                new AppProperties.Admin(null, null, null));
    }
}
