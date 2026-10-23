package com.hikmethankolay.template.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hikmethankolay.template.TestProperties;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;

class ProductionSettingsCheckTest {

    private static final String FRONTEND = "https://app.example.com";

    @Test
    void acceptsRealSettings() {
        assertThat(ProductionSettingsCheck.problems(properties(FRONTEND, FRONTEND, FRONTEND + "/cb", true), "s3cret"))
                .isEmpty();
    }

    @Test
    void namesEveryDevelopmentDefault() {
        String local = "http://localhost:3000";

        List<String> problems =
                ProductionSettingsCheck.problems(properties(local, local, local + "/oauth2/callback", true), "app");

        assertThat(problems).hasSize(4);
        assertThat(String.join("\n", problems))
                .contains("CORS_ALLOWED_ORIGINS", "MAIL_LINK_BASE_URL", "OAUTH2_REDIRECT_URI", "DB_PASSWORD");
    }

    @Test
    void oneLocalOriginAmongRealOnesIsAProblem() {
        AppProperties p = properties(FRONTEND, FRONTEND, null, false);
        AppProperties mixed = new AppProperties(
                p.jwt(),
                p.cookie(),
                new AppProperties.Cors(List.of(FRONTEND, "http://127.0.0.1:5173")),
                p.login(),
                p.rateLimit(),
                p.account(),
                p.mail(),
                p.admin(),
                p.mfa(),
                p.socialLogin());

        assertThat(ProductionSettingsCheck.problems(mixed, "s3cret"))
                .singleElement()
                .asString()
                .contains("CORS_ALLOWED_ORIGINS");
    }

    @Test
    void aLocalRedirectUriIsFineWithoutSocialLogin() {
        assertThat(ProductionSettingsCheck.problems(
                        properties(FRONTEND, FRONTEND, "http://localhost:3000/oauth2/callback", false), "s3cret"))
                .isEmpty();
    }

    @Test
    void recognisesLocalHosts() {
        assertThat(List.of(
                        "http://localhost:3000",
                        "http://LOCALHOST",
                        "http://app.localhost:8080",
                        "http://127.0.0.1",
                        "http://[::1]:3000",
                        "http://0.0.0.0:3000",
                        " http://localhost "))
                .allMatch(ProductionSettingsCheck::isLocal);
        assertThat(List.of(
                        "https://app.example.com",
                        "https://localhost.example.com",
                        "https://my-localhost.dev",
                        "not a url",
                        "*"))
                .noneMatch(ProductionSettingsCheck::isLocal);
        assertThat(ProductionSettingsCheck.isLocal(null)).isFalse();
    }

    @Test
    void refusesToStartWithDevelopmentSettings() {
        AppProperties local = TestProperties.create();
        JdbcConnectionDetails database = database("app");

        assertThatThrownBy(() -> new ProductionSettingsCheck(local, database))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("prod profile")
                .hasMessageContaining("DB_PASSWORD");
    }

    @Test
    void startsWithRealSettings() {
        AppProperties real = properties(FRONTEND, FRONTEND, null, false);

        new ProductionSettingsCheck(real, database("s3cret"));
    }

    private static AppProperties properties(String origin, String linkBaseUrl, String redirectUri, boolean github) {
        AppProperties p = TestProperties.create();
        AppProperties.SocialLogin.Client none = new AppProperties.SocialLogin.Client(null, null);
        return new AppProperties(
                p.jwt(),
                p.cookie(),
                new AppProperties.Cors(List.of(origin)),
                p.login(),
                p.rateLimit(),
                p.account(),
                new AppProperties.Mail("no-reply@example.com", linkBaseUrl, 10, Duration.ofSeconds(30)),
                p.admin(),
                p.mfa(),
                new AppProperties.SocialLogin(
                        redirectUri, github ? new AppProperties.SocialLogin.Client("id", "secret") : none, none));
    }

    private static JdbcConnectionDetails database(String password) {
        return new JdbcConnectionDetails() {
            @Override
            public String getUsername() {
                return "app";
            }

            @Override
            public String getPassword() {
                return password;
            }

            @Override
            public String getJdbcUrl() {
                return "jdbc:postgresql://db:5432/app";
            }
        };
    }
}
