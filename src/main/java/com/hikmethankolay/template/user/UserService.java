package com.hikmethankolay.template.user;

import com.hikmethankolay.template.audit.AuditEvent;
import com.hikmethankolay.template.audit.AuditLog;
import com.hikmethankolay.template.common.ApiException;
import com.hikmethankolay.template.common.ProblemType;
import com.hikmethankolay.template.common.SortAllowList;
import com.hikmethankolay.template.config.CacheConfig.CacheNames;
import com.hikmethankolay.template.user.dto.AdminUpdateUserRequest;
import com.hikmethankolay.template.user.dto.ChangePasswordRequest;
import com.hikmethankolay.template.user.dto.DeleteAccountRequest;
import com.hikmethankolay.template.user.dto.UpdateProfileRequest;
import com.hikmethankolay.template.user.dto.UserResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cache.annotation.Cacheable;
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
 *
 * <p>Changing a password or role ends all of the user's sessions (see {@link SessionsInvalidated}).</p>
 *
 * <p>Lookups by id are cached in {@link CacheNames#USERS}; every method that changes a user
 * evicts its entry. Evictions take effect after the transaction commits (see {@code CacheConfig}).</p>
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    /**
     * Properties users can be sorted by: only fields already visible in {@link UserResponse}, never
     * {@code passwordHash}. Listed in the {@code sort} parameter description of the API.
     */
    public static final String SORTABLE_PROPERTIES = "id, username, email, role, emailVerified, createdAt, updatedAt";

    /** Rejects sorting by anything outside {@link #SORTABLE_PROPERTIES}. */
    private static final SortAllowList SORTING = new SortAllowList(List.of(SORTABLE_PROPERTIES.split(", ")));

    /** User persistence. */
    private final UserRepository userRepository;

    /** Hashes and verifies passwords. */
    private final PasswordEncoder passwordEncoder;

    /** Publishes {@link EmailVerificationRequired} and {@link SessionsInvalidated} events. */
    private final ApplicationEventPublisher events;

    /** Records security events. */
    private final AuditLog audit;

    /**
     * Creates the service.
     *
     * @param userRepository  user persistence
     * @param passwordEncoder hashes and verifies passwords
     * @param events          publishes user events
     * @param audit           records security events
     */
    public UserService(
            UserRepository userRepository, PasswordEncoder passwordEncoder, ApplicationEventPublisher events, AuditLog audit) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.events = events;
        this.audit = audit;
    }

    /**
     * Creates a user. Unverified users get a verification email after the transaction commits.
     *
     * @param username      unique username
     * @param email         unique email; stored trimmed and in lower case
     * @param rawPassword   plain password, stored hashed
     * @param role          role of the new user
     * @param emailVerified whether the address is already trusted (e.g. the configured admin)
     * @return the created user
     * @throws ApiException 409 if the username or email is already taken
     */
    @Transactional
    public UserResponse register(String username, String email, String rawPassword, Role role, boolean emailVerified) {
        String normalizedEmail = normalizeEmail(email);
        ensureUsernameAvailable(username);
        ensureEmailAvailable(normalizedEmail);

        User user = new User(username, normalizedEmail, passwordEncoder.encode(rawPassword), role);
        user.setEmailVerified(emailVerified);
        User saved = userRepository.save(user);
        audit.record(AuditEvent.USER_REGISTERED, saved.getId(), "role", role);
        if (!emailVerified) {
            events.publishEvent(new EmailVerificationRequired(saved.getId()));
        }
        return UserResponse.from(saved);
    }

    /**
     * Marks a user's email address as verified.
     *
     * @param id user id
     * @throws ApiException 404 if the user does not exist
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.USERS, key = "#id")
    public void markEmailVerified(long id) {
        findUser(id).setEmailVerified(true);
        audit.record(AuditEvent.EMAIL_VERIFIED, id);
    }

    /**
     * Sets a new password without checking the old one, e.g. after a verified password reset.
     * Ends all of the user's sessions.
     *
     * @param id          user id
     * @param rawPassword the new plain password, stored hashed
     * @throws ApiException 404 if the user does not exist
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.USERS, key = "#id")
    public void setPassword(long id, String rawPassword) {
        findUser(id).setPasswordHash(passwordEncoder.encode(rawPassword));
        events.publishEvent(new SessionsInvalidated(id));
    }

    /**
     * Looks up a user.
     *
     * @param id user id
     * @return the user
     * @throws ApiException 404 if the user does not exist (not cached)
     */
    @Cacheable(cacheNames = CacheNames.USERS, key = "#id")
    public UserResponse getById(long id) {
        return UserResponse.from(findUser(id));
    }

    /**
     * Lists users page by page.
     *
     * @param pageable page number, size and sort order
     * @return one page of users
     * @throws ApiException 400 if the sort order uses a property outside {@link #SORTABLE_PROPERTIES}
     */
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(SORTING.check(pageable)).map(UserResponse::from);
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
    @CacheEvict(cacheNames = CacheNames.USERS, key = "#id")
    public UserResponse updateProfile(long id, UpdateProfileRequest request) {
        User user = findUser(id);
        applyIdentityChanges(user, request.username(), request.email());
        return UserResponse.from(userRepository.saveAndFlush(user));
    }

    /**
     * Changes a user's password after verifying the current one. Ends all of the user's sessions,
     * including the current one: the client must log in again.
     *
     * @param id      user id
     * @param request current and new password
     * @throws ApiException 404 if the user does not exist, 400 if the current password is wrong
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.USERS, key = "#id")
    public void changePassword(long id, ChangePasswordRequest request) {
        User user = findUser(id);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.badRequest(ProblemType.WRONG_PASSWORD, "Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        audit.record(AuditEvent.PASSWORD_CHANGED, id);
        events.publishEvent(new SessionsInvalidated(id));
    }

    /**
     * Updates any field of a user as an administrator; {@code null} fields are left unchanged.
     * A new password or role ends all of the user's sessions, so it takes effect immediately.
     *
     * @param id      user id
     * @param request the changes
     * @return the updated user
     * @throws ApiException 404 if the user does not exist, 409 if the new username or email is taken or
     *                      the change would demote the last administrator
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.USERS, key = "#id")
    public UserResponse update(long id, AdminUpdateUserRequest request) {
        User user = findUser(id);
        boolean roleChanged = request.role() != null && request.role() != user.getRole();
        if (roleChanged && user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) == 1) {
            throw ApiException.conflict(ProblemType.LAST_ADMINISTRATOR,
                    "The last administrator cannot lose the ADMIN role.");
        }
        String oldUsername = user.getUsername();
        String oldEmail = user.getEmail();
        applyIdentityChanges(user, request.username(), request.email());
        boolean identityChanged = !user.getUsername().equals(oldUsername) || !user.getEmail().equals(oldEmail);
        if (request.password() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (identityChanged || request.password() != null) {
            audit.record(AuditEvent.USER_UPDATED_BY_ADMIN, id,
                    "identityChanged", identityChanged, "passwordChanged", request.password() != null);
        }
        if (roleChanged) {
            audit.record(AuditEvent.ROLE_CHANGED, id, "from", user.getRole(), "to", request.role());
            user.setRole(request.role());
        }
        if (roleChanged || request.password() != null) {
            events.publishEvent(new SessionsInvalidated(id));
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
    @CacheEvict(cacheNames = CacheNames.USERS, key = "#id")
    public void delete(long id, long requesterId) {
        if (id == requesterId) {
            throw ApiException.badRequest(ProblemType.OWN_ACCOUNT, "You cannot delete your own account.");
        }
        userRepository.delete(findUser(id));
        audit.record(AuditEvent.USER_DELETED_BY_ADMIN, id);
        events.publishEvent(new SessionsInvalidated(id));
    }

    /**
     * Deletes the caller's own account after checking the password. Data of other features goes
     * with it through {@code ON DELETE CASCADE}; the sessions end.
     *
     * @param id      the caller's user id
     * @param request the current password
     * @throws ApiException 400 if the password is wrong, 404 if the user does not exist,
     *                      409 if the user is the last administrator
     */
    @Transactional
    @CacheEvict(cacheNames = CacheNames.USERS, key = "#id")
    public void deleteOwnAccount(long id, DeleteAccountRequest request) {
        User user = findUser(id);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.badRequest(ProblemType.WRONG_PASSWORD, "Password is incorrect.");
        }
        if (user.getRole() == Role.ADMIN && userRepository.countByRole(Role.ADMIN) == 1) {
            throw ApiException.conflict(ProblemType.LAST_ADMINISTRATOR, "The last administrator cannot delete their account.");
        }
        userRepository.delete(user);
        audit.record(AuditEvent.ACCOUNT_DELETED, id);
        events.publishEvent(new SessionsInvalidated(id));
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
                .orElseThrow(() -> ApiException.notFound(ProblemType.USER_NOT_FOUND, "User " + id + " not found."));
    }

    /**
     * Applies username and email changes after checking that they are free. A changed email is
     * marked unverified and triggers a new verification email.
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
                // A new address must be confirmed again.
                user.setEmailVerified(false);
                events.publishEvent(new EmailVerificationRequired(user.getId()));
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
            throw ApiException.conflict(ProblemType.USERNAME_TAKEN, "Username is already taken.");
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
            throw ApiException.conflict(ProblemType.EMAIL_TAKEN, "Email is already registered.");
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
