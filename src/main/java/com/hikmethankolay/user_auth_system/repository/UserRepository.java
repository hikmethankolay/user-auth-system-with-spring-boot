package com.hikmethankolay.user_auth_system.repository;

import com.hikmethankolay.user_auth_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;


import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @NonNull
    Page<User> findAll(@NonNull Pageable pageable);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
}
