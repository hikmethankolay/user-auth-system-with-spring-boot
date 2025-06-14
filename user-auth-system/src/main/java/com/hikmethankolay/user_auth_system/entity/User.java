/**
 * @file User.java
 * @brief Entity class representing system users.
 *
 * This entity defines users in the system, including authentication details and roles.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.entity
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;

/**
 * @class User
 * @brief Entity representing a system user.
 *
 * This class defines the structure of a user, including authentication credentials and assigned role.
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

    /** User ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username of the user. */
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    /** Email of the user. */
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    /** Encrypted password of the user. */
    @Column(name = "password", nullable = false)
    private String password;

    /** Role assigned to the user. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    /**
     * @brief Constructor with user details.
     * @param username The username of the user.
     * @param email The email of the user.
     * @param password The password of the user.
     */
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * @brief Default constructor.
     */
    public User() {}

    /**
     * @brief Gets the user ID.
     * @return The ID of the user.
     */
    public Long getId() { return id; }

    /**
     * @brief Sets the user ID.
     * @param id The ID to be set.
     */
    public void setId(Long id) { this.id = id; }

    /**
     * @brief Gets the username of the user.
     * @return The username.
     */
    public String getUsername() { return username; }

    /**
     * @brief Sets the username of the user.
     * @param username The username to be set.
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * @brief Gets the email of the user.
     * @return The email.
     */
    public String getEmail() { return email; }

    /**
     * @brief Sets the email of the user.
     * @param email The email to be set.
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * @brief Gets the encrypted password of the user.
     * @return The password.
     */
    public String getPassword() { return password; }

    /**
     * @brief Sets the encrypted password of the user.
     * @param password The password to be set.
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * @brief Gets the role assigned to the user.
     * @return The role.
     */
    public Role getRole() { return role; }

    /**
     * @brief Sets the role assigned to the user.
     * @param role The role to be assigned.
     */
    public void setRole(Role role) { this.role = role; }

    /**
     * @brief Gets the authorities (roles) assigned to the user.
     * @return A collection of granted authorities.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role != null) {
            return List.of(new SimpleGrantedAuthority(role.getName().name()));
        }
        return List.of();
    }

    /**
     * @brief Converts the User object to a string representation.
     * @return A string representation of User.
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                '}';
    }
}
