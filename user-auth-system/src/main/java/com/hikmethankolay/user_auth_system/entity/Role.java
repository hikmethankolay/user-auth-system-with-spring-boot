/**
 * @file Role.java
 * @brief Entity class representing user roles.
 *
 * This entity defines user roles in the system and their relationships with users.
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
import java.util.ArrayList;
import java.util.List;

/**
 * @class Role
 * @brief Entity representing a user role.
 *
 * This class defines the structure of roles, including their ID, name, and associated users.
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

    /** List of users associated with the role. */
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users = new ArrayList<>();

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
    public void setId(int id) {
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
     * @brief Gets the list of users associated with the role.
     * @return List of users.
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * @brief Sets the list of users associated with the role.
     * @param users The list of users to be assigned.
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * @brief Adds a user to the role.
     * @param user The user to be added.
     */
    public void addUser(User user) {
        users.add(user);
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
