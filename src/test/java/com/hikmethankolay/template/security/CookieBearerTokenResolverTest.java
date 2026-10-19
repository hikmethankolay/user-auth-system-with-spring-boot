package com.hikmethankolay.template.security;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

class CookieBearerTokenResolverTest {

    private final CookieBearerTokenResolver resolver = new CookieBearerTokenResolver("auth_token");

    @Test
    void prefersAuthorizationHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer header-token");
        request.setCookies(new Cookie("auth_token", "cookie-token"));

        assertThat(resolver.resolve(request)).isEqualTo("header-token");
    }

    @Test
    void fallsBackToCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("auth_token", "cookie-token"));

        assertThat(resolver.resolve(request)).isEqualTo("cookie-token");
    }

    @Test
    void returnsNullWithoutTokenOrWithEmptyCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertThat(resolver.resolve(request)).isNull();

        request.setCookies(new Cookie("auth_token", ""));
        assertThat(resolver.resolve(request)).isNull();
    }
}
