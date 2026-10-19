package com.hikmethankolay.template.account;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hikmethankolay.template.IntegrationTest;
import com.hikmethankolay.template.TestMailConfiguration.CapturingMailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

/** With app.account.require-verified-email=true (the prod default), unverified users cannot log in. */
@TestPropertySource(properties = "app.account.require-verified-email=true")
class VerifiedEmailRequiredIntegrationTest extends IntegrationTest {

    @Autowired
    private CapturingMailSender mail;

    @Test
    void unverifiedUserCannotLogInUntilVerified() throws Exception {
        String username = uniqueUsername();
        register(username);
        String loginBody = json("""
                {"identifier":"%s","password":"%s"}""", username, PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Email address is not verified."));

        String token = CapturingMailSender.token(mail.latest(username + "@example.com", "Confirm your email").orElseThrow());
        mockMvc.perform(post("/api/v1/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"token":"%s"}""", token)))
                .andExpect(status().isNoContent());

        login(username, PASSWORD);
    }

    @Test
    void configuredAdminIsVerifiedFromTheStart() throws Exception {
        login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }
}
