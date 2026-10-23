package com.hikmethankolay.template.mfa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A single-use recovery code, stored as a SHA-256 hash ({@code recovery_codes} table). Codes are only ever
 * inserted and then marked used by {@link RecoveryCodeRepository#markUsed}, so the entity has no accessors.
 */
@Entity
@Table(name = "recovery_codes")
public class RecoveryCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "code_hash", nullable = false, updatable = false, length = 64)
    private String codeHash;

    /** Constructor required by JPA. */
    protected RecoveryCode() {}

    /**
     * Creates an unused code.
     *
     * @param userId   the owner
     * @param codeHash SHA-256 of the normalized code
     */
    public RecoveryCode(long userId, String codeHash) {
        this.userId = userId;
        this.codeHash = codeHash;
    }
}
