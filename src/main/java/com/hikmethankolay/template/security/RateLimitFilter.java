package com.hikmethankolay.template.security;

import com.hikmethankolay.template.common.ApiException;
import com.hikmethankolay.template.common.ProblemType;
import com.hikmethankolay.template.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Limits every client IP to {@code app.rate-limit.requests-per-minute} requests per one-minute window.
 *
 * <p>Counters live in Redis, so the limit holds across all instances. Every response carries the
 * IETF draft headers {@code RateLimit-Limit}, {@code RateLimit-Remaining} and {@code RateLimit-Reset}
 * (seconds until the window resets). Over the limit, the request is rejected with a
 * {@code 429} problem detail and {@code Retry-After}. Actuator endpoints are exempt so health probes
 * are never throttled, and requests are allowed if Redis is unavailable.</p>
 *
 * <p>The client IP is {@code request.getRemoteAddr()}; behind a proxy, set
 * {@code server.forward-headers-strategy} so it is the real client address.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class RateLimitFilter extends OncePerRequestFilter {

    /** Logger for Redis failures. */
    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    /** Length of one rate-limit window. */
    private static final Duration WINDOW = Duration.ofMinutes(1);

    /** Atomically increments the window counter, setting its expiry on the first request. */
    private static final RedisScript<Long> INCREMENT = RedisScript.of(
            "local count = redis.call('INCR', KEYS[1]) "
                    + "if count == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]) end return count",
            Long.class);

    /** Redis access with string keys and values. */
    private final StringRedisTemplate redis;

    /** Renders the 429 like every other API error. */
    private final HandlerExceptionResolver resolver;

    /** Key prefix, namespaced by application name. */
    private final String keyPrefix;

    /** Whether limiting is enabled. */
    private final boolean enabled;

    /** Requests allowed per window. */
    private final int limit;

    /**
     * Creates the filter.
     *
     * @param redis           Redis access
     * @param resolver        Spring MVC's primary exception resolver
     * @param properties      application properties holding the limit
     * @param applicationName value of {@code spring.application.name}, used to namespace keys
     */
    public RateLimitFilter(
            StringRedisTemplate redis,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
            AppProperties properties,
            @Value("${spring.application.name}") String applicationName) {
        this.redis = redis;
        this.resolver = resolver;
        this.keyPrefix = applicationName + ":rate-limit:";
        this.enabled = properties.rateLimit().enabled();
        this.limit = properties.rateLimit().requestsPerMinute();
    }

    /**
     * Skips the filter when limiting is disabled and for Actuator endpoints.
     *
     * @param request the current request
     * @return {@code true} if the request is not limited
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled || request.getRequestURI().startsWith(request.getContextPath() + "/actuator");
    }

    /**
     * Counts the request and either passes it on or rejects it with 429.
     *
     * @param request     the current request
     * @param response    the current response
     * @param filterChain the remaining filters
     * @throws ServletException if a later filter fails
     * @throws IOException      if writing the response fails
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long nowSeconds = Instant.now().getEpochSecond();
        long window = nowSeconds / WINDOW.toSeconds();
        long resetSeconds = (window + 1) * WINDOW.toSeconds() - nowSeconds;

        Long count;
        try {
            count = redis.execute(INCREMENT, List.of(keyPrefix + request.getRemoteAddr() + ":" + window),
                    Long.toString(WINDOW.toMillis()));
        } catch (DataAccessException ex) {
            log.warn("Rate limit check failed, allowing request: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        response.setHeader("RateLimit-Limit", Integer.toString(limit));
        response.setHeader("RateLimit-Remaining", Long.toString(Math.max(0, limit - count)));
        response.setHeader("RateLimit-Reset", Long.toString(resetSeconds));

        if (count > limit) {
            resolver.resolveException(request, response, null, ApiException.tooManyRequests(ProblemType.RATE_LIMITED,
                    "Rate limit exceeded. Try again in " + resetSeconds + " seconds.", Duration.ofSeconds(resetSeconds)));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
