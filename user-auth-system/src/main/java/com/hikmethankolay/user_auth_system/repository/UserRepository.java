/**
 * @file UserRepository.java
 * @brief Repository interface for User entity.
 *
 * This interface defines methods for retrieving and managing User entities in the database.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.repository
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.repository;

import com.hikmethankolay.user_auth_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import java.util.Optional;

/**
 * @interface UserRepository
 * @brief Repository interface for managing User entities.
 *
 * This interface extends JpaRepository to provide CRUD operations for User entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * @brief Retrieves all users with pagination support.
     * @param pageable Pagination information.
     * @return A paginated list of users.
     */
    @NonNull
    Page<User> findAll(@NonNull Pageable pageable);

    /**
     * @brief Finds a user by username.
     * @param username The username of the user.
     * @return An Optional containing the User if found.
     */
    Optional<User> findByUsername(String username);

    /**
     * @brief Finds a user by email.
     * @param email The email of the user.
     * @return An Optional containing the User if found.
     */
    Optional<User> findByEmail(String email);

    /**
     * @brief Finds a user by either username or email.
     * @param username The username of the user.
     * @param email The email of the user.
     * @return An Optional containing the User if found.
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
}