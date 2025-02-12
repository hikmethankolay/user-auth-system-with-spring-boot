/**
 * @file RoleService.java
 * @brief Service class for role management.
 *
 * This class provides methods to retrieve roles from the database.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.service
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.service;

import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.repository.RoleRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * @class RoleService
 * @brief Service class for handling role-related operations.
 *
 * This service interacts with the RoleRepository to fetch role details.
 */
@Service
public class RoleService {

    /** Repository for role data access. */
    private final RoleRepository roleRepository;

    /**
     * @brief Constructor for RoleService.
     * @param roleRepository The role repository instance.
     */
    RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * @brief Retrieves all roles.
     * @return A list of all roles.
     */
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    /**
     * @brief Finds a role by its name.
     * @param name The name of the role.
     * @return An Optional containing the role if found.
     */
    public Optional<Role> findByName(ERole name) {
        return roleRepository.findByName(name);
    }
}