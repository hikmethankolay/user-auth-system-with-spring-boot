package com.hikmethankolay.template.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Forwards security filter errors (401/403) to Spring MVC exception handling, so they are rendered
 * by {@code GlobalExceptionHandler} exactly like errors thrown from controllers.
 */
@Component
class SecurityProblemHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    /** Spring MVC's composite resolver, which includes the {@code @RestControllerAdvice} handlers. */
    private final HandlerExceptionResolver resolver;

    /**
     * Creates the handler.
     *
     * @param resolver Spring MVC's primary exception resolver
     */
    SecurityProblemHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Called when a request needs authentication but has no valid token.
     *
     * @param request  the rejected request
     * @param response the response to write the 401 problem to
     * @param ex       why authentication failed
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) {
        resolver.resolveException(request, response, null, ex);
    }

    /**
     * Called when an authenticated user lacks the required authority.
     *
     * @param request  the rejected request
     * @param response the response to write the 403 problem to
     * @param ex       why access was denied
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) {
        resolver.resolveException(request, response, null, ex);
    }
}
