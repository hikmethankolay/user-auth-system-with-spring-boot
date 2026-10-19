package com.hikmethankolay.template.user;

/**
 * Application roles.
 *
 * <p>Spring Security sees them as {@code ROLE_<name>}, so check them with
 * {@code @PreAuthorize("hasRole('ADMIN')")}. When adding a role, also extend the
 * {@code ck_users_role} constraint in a new Flyway migration.</p>
 */
public enum Role {
    /** Regular user; can only manage their own account. */
    USER,
    /** Administrator; can manage all users. */
    ADMIN
}
