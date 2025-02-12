/**
 * @file JwtFilter.java
 * @brief JWT authentication filter.
 *
 * This filter validates JWT tokens and sets authentication in the security context.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.security
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.security;

import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.service.UserService;
import com.hikmethankolay.user_auth_system.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Optional;

/**
 * @class JwtFilter
 * @brief Filter for JWT authentication.
 *
 * This class filters incoming requests to validate JWT tokens and authenticate users.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    /** Utility for JWT operations. */
    private final JwtUtils jwtUtils;

    /** User service for retrieving user details. */
    private final UserService userService;

    /**
     * @brief Constructor for JwtFilter.
     * @param jwtUtils The JWT utility instance.
     * @param userService The user service instance.
     */
    public JwtFilter(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    /**
     * @brief Filters incoming requests for JWT authentication.
     * @param request The HTTP request.
     * @param response The HTTP response.
     * @param filterChain The filter chain.
     * @throws ServletException If a servlet exception occurs.
     * @throws IOException If an IO exception occurs.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            if (jwtUtils.validateJwtToken(token)) {

                Long userId = jwtUtils.getUserIdFromJwtToken(token);
                Optional<User> user = userService.findById(userId);

                if (user.isPresent()) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user.get(), null, user.get().getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    request.setAttribute("userId", userId);
                }

            }
        }

        filterChain.doFilter(request, response);
    }
}
