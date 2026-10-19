package com.hikmethankolay.template.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hikmethankolay.template.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class UserIntegrationTest extends IntegrationTest {

    @Test
    void meReturnsCurrentUser() throws Exception {
        String username = uniqueUsername();
        String token = registerAndLogin(username);

        mockMvc.perform(get("/api/v1/users/me").with(bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void updateProfileChangesOnlyProvidedFields() throws Exception {
        String username = uniqueUsername();
        String token = registerAndLogin(username);

        mockMvc.perform(patch("/api/v1/users/me").with(bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"email":"new-%s@example.com"}""", username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value("new-" + username + "@example.com"));
    }

    @Test
    void updateProfileRejectsTakenUsername() throws Exception {
        String taken = uniqueUsername();
        register(taken);
        String token = registerAndLogin(uniqueUsername());

        mockMvc.perform(patch("/api/v1/users/me").with(bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"username":"%s"}""", taken)))
                .andExpect(status().isConflict());
    }

    @Test
    void changePasswordRequiresCurrentPassword() throws Exception {
        String username = uniqueUsername();
        String token = registerAndLogin(username);
        String newPassword = "Changed#456";

        mockMvc.perform(put("/api/v1/users/me/password").with(bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"currentPassword":"Wrong#123","newPassword":"%s"}""", newPassword)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Current password is incorrect."));

        mockMvc.perform(put("/api/v1/users/me/password").with(bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"currentPassword":"%s","newPassword":"%s"}""", PASSWORD, newPassword)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/login").with(fromIp("10.0.1.1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"identifier":"%s","password":"%s"}""", username, PASSWORD)))
                .andExpect(status().isUnauthorized());
        login(username, newPassword);
    }

    @Test
    void userCanReadOwnRecordButNotOthers() throws Exception {
        long otherId = register(uniqueUsername());
        String username = uniqueUsername();
        long ownId = register(username);
        String token = login(username, PASSWORD);

        mockMvc.perform(get("/api/v1/users/{id}", ownId).with(bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
        mockMvc.perform(get("/api/v1/users/{id}", otherId).with(bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUserCannotUseAdminEndpoints() throws Exception {
        String token = registerAndLogin(uniqueUsername());

        mockMvc.perform(get("/api/v1/users").with(bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mockMvc.perform(delete("/api/v1/users/{id}", 1L).with(bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanManageUsers() throws Exception {
        String adminToken = login(ADMIN_USERNAME, ADMIN_PASSWORD);
        long userId = register(uniqueUsername());

        mockMvc.perform(get("/api/v1/users").with(bearer(adminToken)).param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$.page.totalElements").isNumber());

        mockMvc.perform(patch("/api/v1/users/{id}", userId).with(bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"ADMIN"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(delete("/api/v1/users/{id}", userId).with(bearer(adminToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/{id}", userId).with(bearer(adminToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCannotDeleteOwnAccount() throws Exception {
        String adminToken = login(ADMIN_USERNAME, ADMIN_PASSWORD);
        String me = mockMvc.perform(get("/api/v1/users/me").with(bearer(adminToken)))
                .andReturn().getResponse().getContentAsString();
        long adminId = ((Number) JsonPath.read(me, "$.id")).longValue();

        mockMvc.perform(delete("/api/v1/users/{id}", adminId).with(bearer(adminToken)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownSortPropertyIsBadRequest() throws Exception {
        String adminToken = login(ADMIN_USERNAME, ADMIN_PASSWORD);

        mockMvc.perform(get("/api/v1/users").with(bearer(adminToken)).param("sort", "passwordHashX"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletingOwnAccountRequiresThePasswordAndEndsTheSession() throws Exception {
        String username = uniqueUsername();
        String token = registerAndLogin(username);

        mockMvc.perform(delete("/api/v1/users/me").with(bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"Wrong#123"}"""))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/v1/users/me").with(bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"password":"%s"}""", PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/me").with(bearer(token))).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/login").with(fromIp("10.0.3.1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"identifier":"%s","password":"%s"}""", username, PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exportContainsProfileAndSessions() throws Exception {
        String username = uniqueUsername();
        register(username);
        loginTokens(username, PASSWORD);
        String token = login(username, PASSWORD);

        mockMvc.perform(get("/api/v1/users/me/export").with(bearer(token)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"personal-data.json\""))
                .andExpect(jsonPath("$.exportedAt").exists())
                .andExpect(jsonPath("$.profile.username").value(username))
                .andExpect(jsonPath("$.profile.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.related.sessions.length()").value(2))
                .andExpect(jsonPath("$.related.sessions[0].id").exists())
                .andExpect(jsonPath("$.related.sessions[0].expiresAt").exists());
    }
}
