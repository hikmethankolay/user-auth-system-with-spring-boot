package com.hikmethankolay.template.mfa.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A new authenticator secret. Show {@code otpauthUri} as a QR code, or {@code secret} for manual entry, then
 * confirm with a code from the app.
 *
 * @param secret     the shared secret, Base32
 * @param otpauthUri the {@code otpauth://totp/...} URI authenticator apps scan
 */
@Schema(requiredProperties = {"secret", "otpauthUri"})
public record TotpEnrollmentResponse(
        String secret, @Schema(format = "uri") String otpauthUri) {}
