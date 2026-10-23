package com.hikmethankolay.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * The prod profile turns off the API docs, makes the auth cookie Secure, requires verified emails and refuses the
 * localhost defaults, so this test sets real-looking frontend URLs.
 */
@ActiveProfiles({"test", "prod"})
@TestPropertySource(
        properties = {
            "app.cors.allowed-origins=https://app.example.com",
            "app.mail.link-base-url=https://app.example.com",
            "app.social-login.redirect-uri=https://app.example.com/oauth2/callback"
        })
class ProdProfileIntegrationTest extends IntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void productionSettingsAreChecked() {
        assertThat(context.containsBean("productionSettingsCheck")).isTrue();
    }

    @Test
    void apiDocsAreDisabled() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isNotFound());
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isNotFound());
    }

    @Test
    void authCookieIsSecure() throws Exception {
        // The configured admin is verified; new users must confirm their email first in prod.
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"identifier":"%s","password":"%s"}""", ADMIN_USERNAME, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure")));
    }

    @Test
    void unverifiedUsersCannotLogIn() throws Exception {
        String username = uniqueUsername();
        register(username);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"identifier":"%s","password":"%s"}""", username, PASSWORD)))
                .andExpect(status().isForbidden());
    }
}
