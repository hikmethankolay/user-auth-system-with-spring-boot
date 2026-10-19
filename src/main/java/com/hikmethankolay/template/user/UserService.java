package com.hikmethankolay.template.user;

import com.hikmethankolay.template.common.ApiException;
import com.hikmethankolay.template.user.dto.AdminUpdateUserRequest;
import com.hikmethankolay.template.user.dto.ChangePasswordRequest;
import com.hikmethankolay.template.user.dto.UpdateProfileRequest;
import com.hikmethankolay.template.user.dto.UserResponse;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User management: registration, lookups, profile and password changes, administration.
 *
 * <p>Methods return DTOs so entities never leave the transaction. Errors are reported with
 * {@link ApiException}: 404 for unknown users, 409 for duplicate usernames or emails,
 * 400 for invalid operations.</p>
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    /** User persistence. */
    private final UserRepository userRepository;

    /** Hashes and verifies passwords. */
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates the service.
     *
     * @param userRepository  user persistence
     * @param passwordEncoder hashes and verifies passwords
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a user.
     *
     * @param username    unique username
     * @param email       unique email; stored trimmed and in lower case
     * @param rawPassword plain password, stored hashed
     * @param role        role of the new user
     * @return the created user
     * @throws ApiException 409 if the username or email is already taken
     */
    @Transactional
    public UserResponse register(String username, String email, String rawPassword, Role role) {
        String normalizedEmail = normalizeEmail(email);
        ensureUsernameAvailable(username);
        ensureEmailAvailable(normalizedEmail);

        User user = new User(username, normalizedEmail, passwordEncoder.encode(rawPassword), role);
        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Looks up a user.
     *
     * @param id user id
     * @return the user
     * @throws ApiException 404 if the user does not exist
     */
    public UserResponse getById(long id) {
        return UserResponse.from(findUser(id));
    }

    /**
     * Lists users page by page.
     *
     * @param pageable page number, size and sort order
     * @return one page of users
     */
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    /**
     * Tells whether at least one administrator exists.
     *
     * @return {@code true} if an admin exists
     */
    public boolean adminExists() {
        return userRepository.existsByRole(Role.ADMIN);
    }

    /**
     * Updates the username and/or email of a user; {@code null} fields are left unchanged.
     *
     * @param id      user id
     * @param request the changes
     * @return the updated user
     * @throws ApiException 404 if the user does not exist, 409 if the new username or email is taken
     */
    @Transactional
    public UserResponse updateProfile(long id, UpdateProfileRequest request) {
        User user = findUser(id);
        applyIdentityChanges(user, request.username(), request.email());
        return UserResponse.from(userRepository.saveAndFlush(user));
    }

    /**
     * Changes a user's password after verifying the current one.
     *
     * @param id      user id
     * @param request current and new password
     * @throws ApiException 404 if the user does not exist, 400 if the current password is wrong
     */
    @Transactional
    public void changePassword(long id, ChangePasswordRequest request) {
        User user = findUser(id);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    /**
     * Updates any field of a user as an administrator; {@code null} fields are left unchanged.
     *
     * @param id      user id
     * @param request the changes
     * @return the updated user
     * @throws ApiException 404 if the user does not exist, 409 if the new username or email is taken
     */
    @Transactional
    public UserResponse update(long id, AdminUpdateUserRequest request) {
        User user = findUser(id);
        applyIdentityChanges(user, request.username(), request.email());
        if (request.password() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        return UserResponse.from(userRepository.saveAndFlush(user));
    }

    /**
     * Deletes a user.
     *
     * @param id          id of the user to delete
     * @param requesterId id of the administrator making the request
     * @throws ApiException 400 if an administrator tries to delete their own account,
     *                      404 if the user does not exist
     */
    @Transactional
    public void delete(long id, long requesterId) {
        if (id == requesterId) {
            throw ApiException.badRequest("You cannot delete your own account.");
        }
        userRepository.delete(findUser(id));
    }

    /**
     * Loads a user entity.
     *
     * @param id user id
     * @return the entity
     * @throws ApiException 404 if the user does not exist
     */
    private User findUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("User " + id + " not found."));
    }

    /**
     * Applies username and email changes after checking that they are free.
     *
     * @param user     the entity to change
     * @param username new username, or {@code null} to keep the current one
     * @param email    new email, or {@code null} to keep the current one
     */
    private void applyIdentityChanges(User user, String username, String email) {
        if (username != null && !username.equals(user.getUsername())) {
            ensureUsernameAvailable(username);
            user.setUsername(username);
        }
        if (email != null) {
            String normalizedEmail = normalizeEmail(email);
            if (!normalizedEmail.equals(user.getEmail())) {
                ensureEmailAvailable(normalizedEmail);
                user.setEmail(normalizedEmail);
            }
        }
    }

    /**
     * Fails if a username is taken.
     *
     * @param username username to check
     * @throws ApiException 409 if the username is taken
     */
    private void ensureUsernameAvailable(String username) {
        if (userRepository.existsByUsername(username)) {
            throw ApiException.conflict("Username is already taken.");
        }
    }

    /**
     * Fails if an email is taken.
     *
     * @param email normalised email to check
     * @throws ApiException 409 if the email is taken
     */
    private void ensureEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict("Email is already registered.");
        }
    }

    /**
     * Normalises an email for storage and comparison.
     *
     * @param email email as entered
     * @return the trimmed, lower-case email
     */
    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
