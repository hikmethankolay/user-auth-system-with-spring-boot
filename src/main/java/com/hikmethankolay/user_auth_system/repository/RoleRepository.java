package com.hikmethankolay.user_auth_system.repository;

import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
