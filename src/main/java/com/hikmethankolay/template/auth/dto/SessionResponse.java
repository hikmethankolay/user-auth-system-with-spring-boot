package com.hikmethankolay.template.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/**
 * One of the caller's active sessions: a login on some device, kept alive by refreshing.
 *
 * @param id         id to use in {@code DELETE /api/v1/users/me/sessions/{id}}
 * @param current    whether the request was made with this session's access token
 * @param rememberMe whether the session was started with "remember me"
 * @param startedAt  when the session started (the login)
 * @param lastUsedAt the login or the last refresh
 * @param expiresAt  when the session ends unless refreshed
 * @param userAgent  {@code User-Agent} of the login or last refresh; left out if none was sent
 * @param ipAddress  client IP of the login or last refresh; left out if unknown
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(requiredProperties = {"id", "current", "rememberMe", "startedAt", "lastUsedAt", "expiresAt"})
public record SessionResponse(
        UUID id,
        boolean current,
        boolean rememberMe,
        Instant startedAt,
        Instant lastUsedAt,
        Instant expiresAt,
        String userAgent,
        String ipAddress) {}
