package com.hikmethankolay.template.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

/**
 * A registered user, mapped to the {@code users} table created by the Flyway migration {@code V1__create_users.sql}.
 */
@Entity
@Table(name = "users")
public class User {

    /** Database-generated id. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique username. */
    @Column(nullable = false, unique = true, length = 32)
    private String username;

    /** Unique email address, stored in lower case. */
    @Column(nullable = false, unique = true, length = 254)
    private String email;

    /** Encoded password; the plain password is never stored. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    /** Role of the user. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** Whether the user has confirmed their email address. */
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    /** Creation time, set once on insert. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Time of the last update. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Optimistic-lock version: a write based on a stale read fails instead of reverting newer changes. */
    @Version
    @Column(nullable = false)
    private long version;

    /** Bumped when every session must end; refresh tokens issued under an older value are rejected. */
    @Column(name = "session_version", nullable = false)
    private int sessionVersion;

    /** Constructor required by JPA. */
    protected User() {
    }

    /**
     * Creates a new, not yet persisted user.
     *
     * @param username     unique username
     * @param email        unique email, already normalised to lower case
     * @param passwordHash encoded password
     * @param role         role of the user
     */
    public User(String username, String email, String passwordHash, Role role) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    /** Sets both timestamps before the first insert. */
    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    /** Refreshes {@code updatedAt} before every update. */
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Returns the id.
     *
     * @return the database id, or {@code null} before the user is persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Changes the username.
     *
     * @param username the new, unique username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the email address.
     *
     * @return the lower-case email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Changes the email address.
     *
     * @param email the new, unique, lower-case email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the encoded password.
     *
     * @return the password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Changes the encoded password.
     *
     * @param passwordHash the new password hash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Returns the role.
     *
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Changes the role.
     *
     * @param role the new role
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Tells whether the email address has been confirmed.
     *
     * @return {@code true} if the email address is verified
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * Marks the email address as verified or unverified.
     *
     * @param emailVerified the new verification state
     */
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    /**
     * Returns the session version.
     *
     * @return the current session version
     */
    public int getSessionVersion() {
        return sessionVersion;
    }

    /** Ends every session of the user: refresh tokens issued under the current version stop working. */
    public void endSessions() {
        sessionVersion++;
    }

    /**
     * Returns the creation time.
     *
     * @return when the user was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the time of the last update.
     *
     * @return when the user was last changed
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
