package com.hikmethankolay.template.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

/**
 * Reads the JWT from the {@code Authorization: Bearer} header (RFC 6750), falling back to the
 * HttpOnly authentication cookie used by browser clients.
 */
class CookieBearerTokenResolver implements BearerTokenResolver {

    private final BearerTokenResolver headerResolver = new DefaultBearerTokenResolver();

    private final String cookieName;

    /**
     * Creates the resolver.
     *
     * @param cookieName name of the authentication cookie
     */
    CookieBearerTokenResolver(String cookieName) {
        this.cookieName = cookieName;
    }

    /**
     * Extracts the token from the request.
     *
     * @param request the incoming request
     * @return the token, or {@code null} when the request carries none
     */
    @Override
    public String resolve(HttpServletRequest request) {
        String token = headerResolver.resolve(request);
        if (token != null) {
            return token;
        }
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        return cookie != null && StringUtils.hasText(cookie.getValue()) ? cookie.getValue() : null;
    }
}
