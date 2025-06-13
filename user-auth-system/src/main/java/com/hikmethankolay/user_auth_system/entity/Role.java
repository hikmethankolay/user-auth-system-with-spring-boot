/**
 * @file Role.java
 * @brief Entity class representing user roles.
 *
 * This entity defines user roles in the system.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.entity
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.entity;

import com.hikmethankolay.user_auth_system.enums.ERole;
import jakarta.persistence.*;

/**
 * @class Role
 * @brief Entity representing a user role.
 *
 * This class defines the structure of roles.
 */
@Entity
@Table(name = "roles")
public class Role {

    /** Role ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    /** Name of the role. */
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    private ERole name;

    /**
     * @brief Default constructor.
     */
    public Role() {
    }

    /**
     * @brief Constructor with role name.
     * @param name The name of the role.
     */
    public Role(ERole name) {
        this.name = name;
    }

    /**
     * @brief Gets the role ID.
     * @return The role ID.
     */
    public long getId() {
        return id;
    }

    /**
     * @brief Sets the role ID.
     * @param id The ID to be set.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @brief Gets the role name.
     * @return The name of the role.
     */
    public ERole getName() {
        return name;
    }

    /**
     * @brief Sets the role name.
     * @param name The name to be set.
     */
    public void setName(ERole name) {
        this.name = name;
    }

    /**
     * @brief Converts the Role object to a string representation.
     * @return A string representation of Role.
     */
    @Override
    public String toString() {
        return "Role{" +
                "name=" + name +
                ", id=" + id +
                '}';
    }
}
