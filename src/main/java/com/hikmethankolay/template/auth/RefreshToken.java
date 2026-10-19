package com.hikmethankolay.template.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A refresh token, stored only as a SHA-256 hash (table {@code refresh_tokens}).
 *
 * <p>All tokens issued from one login share a {@link #getFamilyId() family id}; the family is
 * what a client sees as a "session".</p>
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    /** Database-generated id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner of the token. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Shared by every token issued from the same login. */
    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    /** Hex SHA-256 of the token; the token itself is never stored. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /** Whether the session was started with "remember me". */
    @Column(name = "remember_me", nullable = false)
    private boolean rememberMe;

    /** When the token stops being accepted. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** When the token was exchanged for a new one, or {@code null} while unused. */
    @Column(name = "used_at")
    private Instant usedAt;

    /** When the token was issued. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Constructor required by JPA. */
    protected RefreshToken() {
    }

    /**
     * Creates a new, unused token record.
     *
     * @param userId     owner of the token
     * @param familyId   session the token belongs to
     * @param tokenHash  hex SHA-256 of the token
     * @param rememberMe whether the session was started with "remember me"
     * @param expiresAt  when the token expires
     */
    public RefreshToken(long userId, UUID familyId, String tokenHash, boolean rememberMe, Instant expiresAt) {
        this.userId = userId;
        this.familyId = familyId;
        this.tokenHash = tokenHash;
        this.rememberMe = rememberMe;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    /**
     * Returns the owner's id.
     *
     * @return the user id
     */
    public long getUserId() {
        return userId;
    }

    /**
     * Returns the session the token belongs to.
     *
     * @return the family id
     */
    public UUID getFamilyId() {
        return familyId;
    }

    /**
     * Tells whether the session was started with "remember me".
     *
     * @return {@code true} for remember-me sessions
     */
    public boolean isRememberMe() {
        return rememberMe;
    }

    /**
     * Returns the expiry time.
     *
     * @return when the token stops being accepted
     */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /**
     * Returns the issue time.
     *
     * @return when the token was issued
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Tells whether the token has already been exchanged.
     *
     * @return {@code true} if the token was used
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * Tells whether the token has expired.
     *
     * @param now the current time
     * @return {@code true} if the token is no longer valid
     */
    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    /**
     * Marks the token as exchanged.
     *
     * @param now the current time
     */
    public void markUsed(Instant now) {
        this.usedAt = now;
    }
}
