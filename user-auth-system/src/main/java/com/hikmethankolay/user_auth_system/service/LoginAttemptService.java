/**
 * @file LoginAttemptService.java
 * @brief Service for tracking and limiting login attempts.
 *
 * This service prevents brute force attacks by tracking failed login attempts
 * and blocking users who exceed the maximum allowed attempts within a time window.
 *
 * @author Hikmethan Kolay
 * @date 2025-03-29
 */

package com.hikmethankolay.user_auth_system.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @class LoginAttemptService
 * @brief Service to prevent brute force attacks by limiting login attempts.
 *
 * This service maintains a time-based cache of login attempts, blocking users
 * who exceed the maximum allowed number of failed attempts within a time period.
 */
@Service
public class LoginAttemptService {
    /** Maximum number of login attempts allowed before blocking. */
    private final int MAX_ATTEMPT = 10;

    /** Cache storing the number of failed attempts by identifier (username or IP). */
    private final LoadingCache<String, Integer> attemptsCache;

    /**
     * @brief Constructor initializing the attempts cache.
     *
     * Sets up a cache that expires entries after 1 hour of inactivity.
     */
    public LoginAttemptService() {
        attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    /**
     * @brief Clears the login attempts count for successful logins.
     * @param key The identifier (username or IP) of the login attempt.
     */
    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
    }

    /**
     * @brief Increments the login attempts count for failed logins.
     * @param key The identifier (username or IP) of the login attempt.
     */
    public void loginFailed(String key) {
        int attempts;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    /**
     * @brief Checks if a user is blocked due to excessive login attempts.
     * @param key The identifier (username or IP) to check.
     * @return True if the user has exceeded the maximum allowed attempts.
     */
    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= MAX_ATTEMPT;
        } catch (ExecutionException e) {
            return false;
        }
    }
} 