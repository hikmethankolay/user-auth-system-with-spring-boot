package com.hikmethankolay.template.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publishes the public keys that verify this application's tokens, so another service can accept
 * them with {@code NimbusJwtDecoder.withJwkSetUri(".../.well-known/jwks.json")} without being able to sign any.
 */
@RestController
@Tag(name = "Authentication")
public class JwksController {

    private final JwtKeys keys;

    /**
     * Creates the controller.
     *
     * @param keys the configured keys
     */
    public JwksController(JwtKeys keys) {
        this.keys = keys;
    }

    /**
     * Returns the public keys as a JWK Set (RFC 7517). Empty with HS256, whose secret cannot be shared.
     *
     * @return the JWK Set
     */
    @GetMapping("/.well-known/jwks.json")
    @Operation(
            operationId = "getJwks",
            summary = "Public keys for verifying access tokens",
            description = "JWK Set with the RS256 public keys; empty when tokens are signed with HS256.")
    public Map<String, Object> jwks() {
        return keys.publicJwks();
    }
}
