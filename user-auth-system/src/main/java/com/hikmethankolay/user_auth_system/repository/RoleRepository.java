/**
 * @file RoleRepository.java
 * @brief Repository interface for Role entity.
 *
 * This interface defines methods for retrieving Role entities from the database.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.repository
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.repository;

import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * @interface RoleRepository
 * @brief Repository interface for managing Role entities.
 *
 * This interface extends JpaRepository to provide CRUD operations for Role entities.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * @brief Finds a Role by its name.
     * @param name The name of the role.
     * @return An Optional containing the Role if found.
     */
    Optional<Role> findByName(ERole name);
}