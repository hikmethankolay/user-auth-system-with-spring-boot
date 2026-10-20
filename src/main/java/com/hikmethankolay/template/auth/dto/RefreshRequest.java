package com.hikmethankolay.template.auth.dto;

import jakarta.validation.constraints.Size;

/**
 * Optional body of {@code POST /api/v1/auth/refresh} and {@code POST /api/v1/auth/logout}.
 * Browsers can omit it: the refresh token cookie is used instead.
 *
 * @param refreshToken the refresh token from the last login or refresh
 */
public record RefreshRequest(@Size(max = 100) String refreshToken) {}
