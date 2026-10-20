package com.hikmethankolay.template.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A single-use token sent by email, stored only as a SHA-256 hash (table {@code account_tokens}).
 */
@Entity
@Table(name = "account_tokens")
public class AccountToken {

    /** Database-generated id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner of the token. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** What the token may be used for. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountTokenType type;

    /** Hex SHA-256 of the token; the token itself is never stored. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /** When the token stops being accepted. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** When the token was issued. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Constructor required by JPA. */
    protected AccountToken() {}

    /**
     * Creates a new token record.
     *
     * @param userId    owner of the token
     * @param type      what the token may be used for
     * @param tokenHash hex SHA-256 of the token
     * @param expiresAt when the token expires
     */
    public AccountToken(long userId, AccountTokenType type, String tokenHash, Instant expiresAt) {
        this.userId = userId;
        this.type = type;
        this.tokenHash = tokenHash;
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
     * Tells whether the token has expired.
     *
     * @param now the current time
     * @return {@code true} if the token is no longer valid
     */
    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}
