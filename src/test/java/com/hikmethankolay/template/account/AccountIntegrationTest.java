package com.hikmethankolay.template.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hikmethankolay.template.IntegrationTest;
import com.hikmethankolay.template.TestMailConfiguration.CapturingMailSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;

class AccountIntegrationTest extends IntegrationTest {

    @Autowired
    private CapturingMailSender mail;

    private SimpleMailMessage latestMail(String username, String subjectPart) {
        return mail.latest(username + "@example.com", subjectPart)
                .orElseThrow(() -> new AssertionError("No '" + subjectPart + "' email for " + username));
    }

    private void postJson(String path, String body, int expectedStatus) throws Exception {
        mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void registrationSendsVerificationEmailAndTokenVerifiesAddress() throws Exception {
        String username = uniqueUsername();
        String token = registerAndLogin(username);
        mockMvc.perform(get("/api/v1/users/me").with(bearer(token)))
                .andExpect(jsonPath("$.emailVerified").value(false));

        SimpleMailMessage verification = latestMail(username, "Confirm your email");
        assertThat(verification.getText()).contains("http://localhost:3000/verify-email?token=");
        String verificationToken = CapturingMailSender.token(verification);

        postJson("/api/v1/auth/verify-email", json("""
                {"token":"%s"}""", verificationToken), 204);
        mockMvc.perform(get("/api/v1/users/me").with(bearer(token)))
                .andExpect(jsonPath("$.emailVerified").value(true));

        // Tokens are single-use.
        postJson("/api/v1/auth/verify-email", json("""
                {"token":"%s"}""", verificationToken), 400);
    }

    @Test
    void unknownTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/auth/verify-email").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"definitely-not-a-real-token"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("The link is invalid or has expired."));
    }

    @Test
    void resendReplacesThePreviousVerificationToken() throws Exception {
        String username = uniqueUsername();
        register(username);
        String first = CapturingMailSender.token(latestMail(username, "Confirm your email"));

        postJson("/api/v1/auth/verify-email/resend", json("""
                {"email":"%s@example.com"}""", username), 202);
        String second = CapturingMailSender.token(latestMail(username, "Confirm your email"));

        assertThat(second).isNotEqualTo(first);
        postJson("/api/v1/auth/verify-email", json("""
                {"token":"%s"}""", first), 400);
        postJson("/api/v1/auth/verify-email", json("""
                {"token":"%s"}""", second), 204);
    }

    @Test
    void passwordResetSetsNewPassword() throws Exception {
        String username = uniqueUsername();
        register(username);

        postJson("/api/v1/auth/password-reset", json("""
                {"email":"%s@example.com"}""", username), 202);
        SimpleMailMessage reset = latestMail(username, "Reset your password");
        assertThat(reset.getText()).contains("http://localhost:3000/reset-password?token=");

        postJson("/api/v1/auth/password-reset/confirm", json("""
                {"token":"%s","newPassword":"Reset#4567"}""", CapturingMailSender.token(reset)), 204);

        login(username, "Reset#4567");
        mockMvc.perform(post("/api/v1/auth/login").with(fromIp("10.0.2.1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"identifier":"%s","password":"%s"}""", username, PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void passwordResetValidatesTheNewPassword() throws Exception {
        String username = uniqueUsername();
        register(username);
        postJson("/api/v1/auth/password-reset", json("""
                {"email":"%s@example.com"}""", username), 202);

        postJson("/api/v1/auth/password-reset/confirm", json("""
                {"token":"%s","newPassword":"weak"}""",
                CapturingMailSender.token(latestMail(username, "Reset your password"))), 400);
    }

    @Test
    void unknownEmailGetsTheSameAnswerAndNoEmail() throws Exception {
        String address = uniqueUsername() + "@example.com";

        postJson("/api/v1/auth/password-reset", json("""
                {"email":"%s"}""", address), 202);
        postJson("/api/v1/auth/verify-email/resend", json("""
                {"email":"%s"}""", address), 202);

        assertThat(mail.latest(address, "")).isEmpty();
    }

    @Test
    void changingEmailRequiresVerificationAgain() throws Exception {
        String username = uniqueUsername();
        String token = registerAndLogin(username);
        postJson("/api/v1/auth/verify-email", json("""
                {"token":"%s"}""", CapturingMailSender.token(latestMail(username, "Confirm your email"))), 204);

        String newAddress = "moved-" + username + "@example.com";
        mockMvc.perform(patch("/api/v1/users/me").with(bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"email":"%s"}""", newAddress)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailVerified").value(false));

        assertThat(mail.latest(newAddress, "Confirm your email")).isPresent();
    }
}
