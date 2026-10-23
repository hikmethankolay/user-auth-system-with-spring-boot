package com.hikmethankolay.template.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stops the {@code prod} profile from starting with the development defaults of {@code application.yml}.
 *
 * <p>Those defaults work locally but break a real deployment quietly: browsers on the real domain are refused by
 * CORS, and every verification and password reset email links to {@code http://localhost:3000}. The check names
 * each variable to set and fails before the application accepts requests.</p>
 */
@Component
@Profile("prod")
class ProductionSettingsCheck {

    /** Password of the local database in {@code application.yml} and {@code compose.yaml}. */
    static final String DEVELOPMENT_DB_PASSWORD = "app";

    /**
     * Runs the check.
     *
     * @param properties application properties
     * @param database   the connection actually used, so a service connection is judged by its own credentials
     * @throws IllegalStateException if a development default is still set
     */
    ProductionSettingsCheck(AppProperties properties, JdbcConnectionDetails database) {
        List<String> problems = problems(properties, database.getPassword());
        if (!problems.isEmpty()) {
            throw new IllegalStateException(
                    "The prod profile is running with development settings: " + String.join("; ", problems));
        }
    }

    /**
     * Lists the development defaults still set.
     *
     * @param properties application properties
     * @param dbPassword password of the database connection
     * @return one message per problem, naming the variable to set; empty when there is none
     */
    static List<String> problems(AppProperties properties, String dbPassword) {
        List<String> problems = new ArrayList<>();
        List<String> origins = properties.cors().allowedOrigins();
        if (origins != null && origins.stream().anyMatch(ProductionSettingsCheck::isLocal)) {
            problems.add("CORS_ALLOWED_ORIGINS must list your frontend's origin, not localhost (" + origins + ")");
        }
        String linkBaseUrl = properties.mail().linkBaseUrl();
        if (isLocal(linkBaseUrl)) {
            problems.add("MAIL_LINK_BASE_URL must be your frontend's URL, not localhost (" + linkBaseUrl + ")");
        }
        AppProperties.SocialLogin social = properties.socialLogin();
        boolean socialLogin = social.github().isConfigured() || social.google().isConfigured();
        if (socialLogin && isLocal(social.redirectUri())) {
            problems.add("OAUTH2_REDIRECT_URI must be your frontend's callback page, not localhost ("
                    + social.redirectUri() + ")");
        }
        if (DEVELOPMENT_DB_PASSWORD.equals(dbPassword)) {
            problems.add("DB_PASSWORD must not be the development password");
        }
        return problems;
    }

    /**
     * Tells whether a URL points to this machine.
     *
     * @param url an origin or URL; {@code null} and unparseable values are not local
     * @return {@code true} for {@code localhost}, {@code *.localhost}, {@code 127.*}, {@code [::1]} and {@code 0.0.0.0}
     */
    static boolean isLocal(String url) {
        if (url == null) {
            return false;
        }
        String host;
        try {
            host = URI.create(url.strip()).getHost();
        } catch (IllegalArgumentException ex) {
            return false;
        }
        if (host == null) {
            return false;
        }
        host = host.toLowerCase(Locale.ROOT);
        return host.equals("localhost")
                || host.endsWith(".localhost")
                || host.startsWith("127.")
                || host.equals("[::1]")
                || host.equals("0.0.0.0");
    }
}
