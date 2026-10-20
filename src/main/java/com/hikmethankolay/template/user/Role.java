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
    /** Administrator; can manage regular users. */
    ADMIN,
    /**
     * The account created by {@code AdminBootstrap}; has every administrator right and is the only one
     * who can change roles and manage administrators. There is at most one, and the API can neither
     * assign this role nor delete or demote its holder.
     */
    OWNER
}
