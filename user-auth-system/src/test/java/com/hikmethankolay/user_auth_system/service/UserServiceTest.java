/**
 * @file UserServiceTest.java
 * @brief Tests for the UserService class.
 *
 * Contains unit tests for all user service operations including registration,
 * authentication, token management, and user updates.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.service;

import com.hikmethankolay.user_auth_system.dto.LoginRequestDTO;
import com.hikmethankolay.user_auth_system.dto.UserRegisterDTO;
import com.hikmethankolay.user_auth_system.dto.UserUpdateDTO;
import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.enums.TokenStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @class UserServiceTest
 * @brief Test class for UserService.
 *
 * This class contains unit tests for all user service operations.
 */
public class UserServiceTest extends BaseServiceTest {

    /**
     * UserService instance to be tested, with mocked dependencies injected.
     */
    @Autowired
    private UserService userService;

    /**
     * Set of mock constraint violations for validation testing.
     */
    private Set<ConstraintViolation<UserRegisterDTO>> violations;

    /**
     * @brief Setup method that runs before each test.
     *
     * Initializes common test objects and mocks.
     */
    @BeforeEach
    public void setup() {
        // Create mock validation violations
        violations = new HashSet<>();

        // Setup common role for tests
        Role userRole = new Role(ERole.ROLE_USER);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
    }

    /**
     * @brief Test successful user registration.
     *
     * Verifies that a user is properly registered with valid input data.
     */
    @Test
    public void testRegisterUserSuccess() {
        // Arrange
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("testuser123");
        registerDTO.setEmail("test@example.com");
        registerDTO.setPassword("P@ssw0rd123!");

        when(validator.validate(registerDTO)).thenReturn(Collections.emptySet());
        when(userRepository.findByUsername(registerDTO.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerDTO.getPassword())).thenReturn("encodedPassword");

        User savedUser = new User(registerDTO.getUsername(), registerDTO.getEmail(), "encodedPassword");
        savedUser.setId(1L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.registerUser(registerDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser123", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());

        verify(userRepository).findByUsername(registerDTO.getUsername());
        verify(userRepository).findByEmail(registerDTO.getEmail());
        verify(passwordEncoder).encode(registerDTO.getPassword());
        verify(userRepository).save(any(User.class));
    }

    /**
     * @brief Test registration with validation errors.
     *
     * Verifies that registration fails with appropriate exceptions when validation errors occur.
     */
    @Test
    public void testRegisterUserValidationError() {
        // Arrange
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("short"); // Too short username
        registerDTO.setEmail("invalid-email"); // Invalid email format
        registerDTO.setPassword("weak"); // Weak password

        ConstraintViolation<UserRegisterDTO> mockViolation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<UserRegisterDTO>> violations = new HashSet<>();
        violations.add(mockViolation);

        when(validator.validate(registerDTO)).thenReturn(violations);

        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userService.registerUser(registerDTO));

        verify(validator).validate(registerDTO);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * @brief Test registration with existing username.
     *
     * Verifies that registration fails with appropriate exceptions when username is already taken.
     */
    @Test
    public void testRegisterUserUsernameExists() {
        // Arrange
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("existinguser");
        registerDTO.setEmail("new@example.com");
        registerDTO.setPassword("P@ssw0rd123!");

        User existingUser = new User("existinguser", "existing@example.com", "password");
        existingUser.setId(2L); // Explicitly set non-null ID to pass the filter condition

        when(validator.validate(registerDTO)).thenReturn(Collections.emptySet());
        when(userRepository.findByUsername(registerDTO.getUsername())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(registerDTO));
        assertEquals("Username is already taken!", exception.getMessage());
    }

    /**
     * @brief Test registration with existing email.
     *
     * Verifies that registration fails with appropriate exceptions when email is already taken.
     */
    @Test
    public void testRegisterUserEmailExists() {
        // Arrange
        UserRegisterDTO registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setEmail("existing@example.com");
        registerDTO.setPassword("P@ssw0rd123!");

        User existingUser = new User("existinguser", "existing@example.com", "password");
        existingUser.setId(2L); // Explicitly set non-null ID to pass the filter condition

        when(validator.validate(registerDTO)).thenReturn(Collections.emptySet());
        when(userRepository.findByUsername(registerDTO.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registerDTO.getEmail())).thenReturn(Optional.of(existingUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.registerUser(registerDTO));
        assertEquals("Email is already taken!", exception.getMessage());

        verify(validator).validate(registerDTO);
        verify(userRepository).findByUsername(registerDTO.getUsername());
        verify(userRepository).findByEmail(registerDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * @brief Test successful user authentication.
     *
     * Verifies that authentication succeeds with valid credentials.
     */
    @Test
    public void testAuthenticateUserSuccess() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password", false);
        String clientIp = "127.0.0.1";

        User user = new User("testuser", "test@example.com", "encodedPassword");
        user.setId(1L);

        when(userRepository.findByUsernameOrEmail(loginRequest.identifier(), loginRequest.identifier()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.password(), user.getPassword())).thenReturn(true);
        when(jwtUtils.generateJwtToken(anyString(), anyString(), eq(false))).thenReturn("valid.jwt.token");
        when(loginAttemptService.isBlocked(anyString())).thenReturn(false);

        // Act
        String token = userService.authenticateUser(loginRequest, clientIp);

        // Assert
        assertNotNull(token);
        assertEquals("valid.jwt.token", token);

        verify(userRepository).findByUsernameOrEmail(loginRequest.identifier(), loginRequest.identifier());
        verify(passwordEncoder).matches(loginRequest.password(), user.getPassword());
        verify(jwtUtils).generateJwtToken(eq("1"), eq("testuser"), eq(false));
        verify(loginAttemptService).loginSucceeded(clientIp);
        verify(loginAttemptService).loginSucceeded(loginRequest.identifier());
    }

    /**
     * @brief Test authentication with invalid credentials.
     *
     * Verifies that authentication fails with invalid username or password.
     */
    @Test
    public void testAuthenticateUserInvalidCredentials() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "wrongpassword", false);
        String clientIp = "127.0.0.1";

        User user = new User("testuser", "test@example.com", "encodedPassword");
        user.setId(1L);

        when(userRepository.findByUsernameOrEmail(loginRequest.identifier(), loginRequest.identifier()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.password(), user.getPassword())).thenReturn(false);
        when(loginAttemptService.isBlocked(anyString())).thenReturn(false);

        // Act
        String token = userService.authenticateUser(loginRequest, clientIp);

        // Assert
        assertNull(token);

        verify(userRepository).findByUsernameOrEmail(loginRequest.identifier(), loginRequest.identifier());
        verify(passwordEncoder).matches(loginRequest.password(), user.getPassword());
        verify(loginAttemptService).loginFailed(clientIp);
        verify(loginAttemptService).loginFailed(loginRequest.identifier());
        verify(jwtUtils, never()).generateJwtToken(anyString(), anyString(), anyBoolean());
    }

    /**
     * @brief Test authentication with blocked IP.
     *
     * Verifies that authentication fails when IP is blocked due to too many failed attempts.
     */
    @Test
    public void testAuthenticateUserIpBlocked() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password", false);
        String clientIp = "127.0.0.1";

        when(loginAttemptService.isBlocked(clientIp)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.authenticateUser(loginRequest, clientIp));

        assertTrue(exception.getMessage().contains("Too many failed login attempts from this IP"));

        verify(loginAttemptService).isBlocked(clientIp);
        verify(userRepository, never()).findByUsernameOrEmail(anyString(), anyString());
    }

    /**
     * @brief Test authentication with blocked account.
     *
     * Verifies that authentication fails when account is blocked due to too many failed attempts.
     */
    @Test
    public void testAuthenticateUserAccountBlocked() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("blockeduser", "password", false);
        String clientIp = "127.0.0.1";

        when(loginAttemptService.isBlocked(clientIp)).thenReturn(false);
        when(loginAttemptService.isBlocked(loginRequest.identifier())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.authenticateUser(loginRequest, clientIp));

        assertTrue(exception.getMessage().contains("Account is temporarily locked"));

        verify(loginAttemptService).isBlocked(clientIp);
        verify(loginAttemptService).isBlocked(loginRequest.identifier());
        verify(userRepository, never()).findByUsernameOrEmail(anyString(), anyString());
    }

    /**
     * @brief Test successful token refresh.
     *
     * Verifies that token refresh succeeds with valid token.
     */
    @Test
    public void testRefreshTokenSuccess() {
        // Arrange
        String oldToken = "old.jwt.token";
        String newToken = "new.jwt.token";

        User user = new User("testuser", "test@example.com", "password");
        user.setId(1L);

        when(jwtUtils.validateJwtToken(oldToken)).thenReturn(TokenStatus.VALID);
        when(jwtUtils.getUserIdFromJwtToken(oldToken)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user)); // Fixed: Now using userRepository
        when(jwtUtils.wasRememberMe(oldToken)).thenReturn(true);
        when(jwtUtils.generateJwtToken(eq("1"), eq("testuser"), eq(true))).thenReturn(newToken);

        // Act
        String result = userService.refreshToken(oldToken);

        // Assert
        assertNotNull(result);
        assertEquals(newToken, result);

        verify(jwtUtils).validateJwtToken(oldToken);
        verify(jwtUtils).getUserIdFromJwtToken(oldToken);
        verify(userRepository).findById(1L); // Fixed: Verify userRepository call
        verify(jwtUtils).wasRememberMe(oldToken);
        verify(jwtUtils).generateJwtToken(eq("1"), eq("testuser"), eq(true));
    }

    /**
     * @brief Test token refresh with invalid token.
     *
     * Verifies that token refresh fails with invalid token.
     */
    @Test
    public void testRefreshTokenInvalid() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(TokenStatus.INVALID);

        // Act
        String result = userService.refreshToken(invalidToken);

        // Assert
        assertNull(result);

        verify(jwtUtils).validateJwtToken(invalidToken);
        verify(jwtUtils, never()).getUserIdFromJwtToken(anyString());
    }

    /**
     * @brief Test token refresh with user not found.
     *
     * Verifies that token refresh fails when user ID from token is not found.
     */
    @Test
    public void testRefreshTokenUserNotFound() {
        // Arrange
        String token = "valid.but.unknown.user.token";

        when(jwtUtils.validateJwtToken(token)).thenReturn(TokenStatus.VALID);
        when(jwtUtils.getUserIdFromJwtToken(token)).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty()); // Fixed: Now using userRepository

        // Act
        String result = userService.refreshToken(token);

        // Assert
        assertNull(result);

        verify(jwtUtils).validateJwtToken(token);
        verify(jwtUtils).getUserIdFromJwtToken(token);
        verify(userRepository).findById(99L); // Fixed: Verify userRepository call
        verify(jwtUtils, never()).generateJwtToken(anyString(), anyString(), anyBoolean());
    }

    /**
     * @brief Test successful user update.
     *
     * Verifies that user update succeeds with valid input data.
     */
    @Test
    public void testUpdateUserSuccess() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("newusername");
        updateDTO.setEmail("new@example.com");
        updateDTO.setPassword("NewP@ssw0rd123!");

        User existingUser = new User("oldusername", "old@example.com", "oldpassword");
        existingUser.setId(1L);

        User requester = new User("admin", "admin@example.com", "adminpassword");
        requester.setId(2L);
        Role adminRole = new Role(ERole.ROLE_ADMIN);
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        requester.setRoles(adminRoles);

        when(validator.validate(updateDTO)).thenReturn(Collections.emptySet());
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));
        when(userRepository.findByUsername(updateDTO.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(updateDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(updateDTO.getPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // Act
        User result = userService.updateUser(updateDTO, 1L, 2L);

        // Assert
        assertNotNull(result);
        assertEquals("newusername", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("newEncodedPassword", result.getPassword());

        verify(validator).validate(updateDTO);
        verify(userRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(userRepository).findByUsername(updateDTO.getUsername());
        verify(userRepository).findByEmail(updateDTO.getEmail());
        verify(passwordEncoder).encode(updateDTO.getPassword());
        verify(userRepository).save(existingUser);
    }

    /**
     * @brief Test user update with validation errors.
     *
     * Verifies that user update fails with appropriate exceptions when validation errors occur.
     */
    @Test
    public void testUpdateUserValidationError() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("short"); // Too short username
        updateDTO.setEmail("invalid-email"); // Invalid email format

        ConstraintViolation<UserUpdateDTO> mockViolation = mock(ConstraintViolation.class);
        Set<ConstraintViolation<UserUpdateDTO>> violations = new HashSet<>();
        violations.add(mockViolation);

        when(validator.validate(updateDTO)).thenReturn(violations);

        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> userService.updateUser(updateDTO, 1L, 2L));

        verify(validator).validate(updateDTO);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * @brief Test user update with existing username.
     *
     * Verifies that user update fails when new username is already taken by another user.
     */
    @Test
    public void testUpdateUserUsernameExists() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("existinguser");
        updateDTO.setEmail("new@example.com");

        User otherUser = new User("existinguser", "other@example.com", "otherpassword");
        otherUser.setId(2L);

        when(validator.validate(updateDTO)).thenReturn(Collections.emptySet());
        when(userRepository.findByUsername(updateDTO.getUsername())).thenReturn(Optional.of(otherUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(updateDTO, 1L, 3L));
        assertEquals("Username is already taken!", exception.getMessage());

        verify(validator).validate(updateDTO);
        verify(userRepository).findByUsername(updateDTO.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * @brief Test user update with existing email.
     *
     * Verifies that user update fails when new email is already taken by another user.
     */
    @Test
    public void testUpdateUserEmailExists() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("newusername");
        updateDTO.setEmail("existing@example.com");

        User existingUser = new User("oldusername", "old@example.com", "oldpassword");
        existingUser.setId(1L);

        User otherUser = new User("otheruser", "existing@example.com", "otherpassword");
        otherUser.setId(2L);

        User requester = new User("admin", "admin@example.com", "adminpassword");
        requester.setId(3L);

        when(validator.validate(updateDTO)).thenReturn(Collections.emptySet());
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findById(3L)).thenReturn(Optional.of(requester));
        when(userRepository.findByUsername(updateDTO.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(updateDTO.getEmail())).thenReturn(Optional.of(otherUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(updateDTO, 1L, 3L));
        assertEquals("Email is already taken!", exception.getMessage());

        verify(validator).validate(updateDTO);
        verify(userRepository).findByUsername(updateDTO.getUsername());
        verify(userRepository).findByEmail(updateDTO.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * @brief Test user update with user not found.
     *
     * Verifies that user update fails when the user to update doesn't exist.
     */
    @Test
    public void testUpdateUserNotFound() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("newusername");
        updateDTO.setEmail("new@example.com");

        User requester = new User("admin", "admin@example.com", "adminpassword");
        requester.setId(2L);

        when(validator.validate(updateDTO)).thenReturn(Collections.emptySet());
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(requester));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(updateDTO, 99L, 2L));
        assertEquals("User not found with id: 99", exception.getMessage());

        verify(validator).validate(updateDTO);
        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * @brief Test user update with requester not found.
     *
     * Verifies that user update fails when the requester doesn't exist.
     */
    @Test
    public void testUpdateUserRequesterNotFound() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("newusername");
        updateDTO.setEmail("new@example.com");

        User existingUser = new User("oldusername", "old@example.com", "oldpassword");
        existingUser.setId(1L);

        when(validator.validate(updateDTO)).thenReturn(Collections.emptySet());
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(updateDTO, 1L, 99L));
        assertEquals("Requester not found with id: 99", exception.getMessage());

        verify(validator).validate(updateDTO);
        verify(userRepository).findById(1L);
        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * @brief Test successful user deletion.
     *
     * Verifies that user deletion succeeds for a valid user ID.
     */
    @Test
    public void testDeleteUserSuccess() {
        // Arrange
        User existingUser = new User("testuser", "test@example.com", "password");
        existingUser.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        // Act
        userService.deleteById(1L, 2L);

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).delete(existingUser);
    }

    /**
     * @brief Test user deletion with user not found.
     *
     * Verifies that user deletion fails when the user doesn't exist.
     */
    @Test
    public void testDeleteUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteById(99L, 2L));
        assertEquals("User not found with id 99", exception.getMessage());

        verify(userRepository).findById(99L);
        verify(userRepository, never()).delete(any(User.class));
    }

    /**
     * @brief Test attempt to delete own account.
     *
     * Verifies that user deletion fails when a user tries to delete their own account.
     */
    @Test
    public void testDeleteOwnAccount() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.deleteById(1L, 1L));
        assertEquals("Cannot delete your own account", exception.getMessage());

        verify(userRepository, never()).findById(anyLong());
        verify(userRepository, never()).delete(any(User.class));
    }

    /**
     * @brief Test finding all users with pagination.
     *
     * Verifies that findAll correctly returns paginated users.
     */
    @Test
    public void testFindAllWithPagination() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        User user1 = new User("user1", "user1@example.com", "password1");
        User user2 = new User("user2", "user2@example.com", "password2");
        List<User> userList = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(userList);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<User> result = userService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        verify(userRepository).findAll(pageable);
    }

    /**
     * @brief Test finding user by ID with success.
     *
     * Verifies that findById correctly returns a user when found.
     */
    @Test
    public void testFindByIdSuccess() {
        // Arrange
        User user = new User("testuser", "test@example.com", "password");
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());

        verify(userRepository).findById(1L);
    }

    /**
     * @brief Test finding user by ID when not found.
     *
     * Verifies that findById correctly returns empty when user not found.
     */
    @Test
    public void testFindByIdNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findById(99L);

        // Assert
        assertFalse(result.isPresent());

        verify(userRepository).findById(99L);
    }

    /**
     * @brief Test finding user by username or email with success.
     *
     * Verifies that findByUsernameOrEmail correctly returns a user when found.
     */
    @Test
    public void testFindByUsernameOrEmailSuccess() {
        // Arrange
        User user = new User("testuser", "test@example.com", "password");

        when(userRepository.findByUsernameOrEmail("testuser", "testuser")).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findByUsernameOrEmail("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());

        verify(userRepository).findByUsernameOrEmail("testuser", "testuser");
    }

    /**
     * @brief Test finding user by username or email when not found.
     *
     * Verifies that findByUsernameOrEmail correctly returns empty when user not found.
     */
    @Test
    public void testFindByUsernameOrEmailNotFound() {
        // Arrange
        when(userRepository.findByUsernameOrEmail("nonexistent", "nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByUsernameOrEmail("nonexistent");

        // Assert
        assertFalse(result.isPresent());

        verify(userRepository).findByUsernameOrEmail("nonexistent", "nonexistent");
    }
}