package com.hikmethankolay.template.auth;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hikmethankolay.template.IntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class AuthIntegrationTest extends IntegrationTest {

    @Test
    void registerReturnsCreatedUserWithoutPassword() throws Exception {
        String username = uniqueUsername();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"username":"%s","email":"%s@EXAMPLE.com","password":"%s"}""", username, username, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern("http://localhost/api/v1/users/\\d+")))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(username + "@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registerRejectsInvalidInputWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"a b","email":"not-an-email","password":"weak"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors[?(@.field == 'username')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'email')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'password')]").exists());
    }

    @Test
    void registerRejectsDuplicateUsername() throws Exception {
        String username = uniqueUsername();
        register(username);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"username":"%s","email":"other-%s@example.com","password":"%s"}""", username, username, PASSWORD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Username is already taken."));
    }

    @Test
    void loginWithEmailReturnsTokenAndSessionCookie() throws Exception {
        String username = uniqueUsername();
        register(username);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"identifier":"%s@Example.com","password":"%s"}""", username, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, allOf(
                        containsString("auth_token="), containsString("HttpOnly"),
                        containsString("SameSite=Strict"), not(containsString("Max-Age")))));
    }

    @Test
    void rememberMeLoginReturnsPersistentCookie() throws Exception {
        String username = uniqueUsername();
        register(username);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"identifier":"%s","password":"%s","rememberMe":true}""", username, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresIn").value(2_592_000))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=2592000")));
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        String username = uniqueUsername();
        register(username);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(fromIp("10.0.0.1"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"identifier":"%s","password":"Wrong#123"}""", username)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username or password."));
    }

    @Test
    void repeatedFailedLoginsAreBlocked() throws Exception {
        String username = uniqueUsername();
        register(username);
        String wrongLogin = json("""
                {"identifier":"%s","password":"Wrong#123"}""", username);

        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/api/v1/auth/login").with(fromIp("10.0.0.2"))
                            .contentType(MediaType.APPLICATION_JSON).content(wrongLogin))
                    .andExpect(status().isUnauthorized());
        }

        // Even the correct password is rejected while the account is locked, from any IP.
        mockMvc.perform(post("/api/v1/auth/login").with(fromIp("10.0.0.3"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"identifier":"%s","password":"%s"}""", username, PASSWORD)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "3600"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void protectedEndpointWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void tamperedTokenIsUnauthorized() throws Exception {
        String token = registerAndLogin(uniqueUsername());

        mockMvc.perform(get("/api/v1/users/me").with(bearer(token + "x")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authCookieAuthenticatesRequests() throws Exception {
        String username = uniqueUsername();
        String token = registerAndLogin(username);

        mockMvc.perform(get("/api/v1/users/me").cookie(new Cookie("auth_token", token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void staleCookieDoesNotBlockLogin() throws Exception {
        String username = uniqueUsername();
        register(username);

        mockMvc.perform(post("/api/v1/auth/login")
                        .cookie(new Cookie("auth_token", "expired-or-garbage"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"identifier":"%s","password":"%s"}""", username, PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void refreshIssuesNewToken() throws Exception {
        String token = registerAndLogin(uniqueUsername());

        mockMvc.perform(post("/api/v1/auth/refresh").with(bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    void refreshWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutClearsCookie() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }

    @Test
    void healthAndApiDocsArePublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("spring-boot-template"));
    }
}
