/**
 * @file RoleInfoDTO.java
 * @brief Data Transfer Object for role information.
 *
 * This DTO represents role details including associated users.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.dto
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.dto;

import com.hikmethankolay.user_auth_system.entity.Role;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO representing role information.
 * @param name The name of the role.
 * @param users The users assigned to the role.
 * @author Hikmethan Kolay
 */
public record RoleInfoDTO(
        String name,
        Set<RoleUserInfoDTO> users
) {
    /**
     * @brief Constructs a RoleInfoDTO from a Role entity.
     * @param role The role entity.
     */
    public RoleInfoDTO(Role role) {
        this(
                role.getName().name(),
                role.getUsers() != null
                        ? role.getUsers().stream().map(RoleUserInfoDTO::new).collect(Collectors.toSet())
                        : Set.of()
        );
    }
}
