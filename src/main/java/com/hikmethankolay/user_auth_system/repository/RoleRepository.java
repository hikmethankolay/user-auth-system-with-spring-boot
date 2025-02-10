package com.hikmethankolay.user_auth_system.repository;

import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.enums.ERole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @NonNull
    Page<Role> findAll(@NonNull Pageable pageable);
    Optional<Role> findByName(ERole name);
}
