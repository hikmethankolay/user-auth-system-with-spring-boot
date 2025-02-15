/**
 * @file UserServiceTest.java
 * @brief Unit tests for the UserService class.
 *
 * This file contains various test cases for the UserService class, including user registration,
 * authentication, update operations, and deletion functionality.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-15
 */

/**
 * @package com.hikmethankolay.user_auth_system
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system;

import com.hikmethankolay.user_auth_system.dto.LoginRequestDTO;
import com.hikmethankolay.user_auth_system.dto.UserInfoDTO;
import com.hikmethankolay.user_auth_system.dto.UserUpdateDTO;
import com.hikmethankolay.user_auth_system.entity.Role;
import com.hikmethankolay.user_auth_system.entity.User;
import com.hikmethankolay.user_auth_system.enums.ERole;
import com.hikmethankolay.user_auth_system.repository.UserRepository;
import com.hikmethankolay.user_auth_system.service.UserService;
import com.hikmethankolay.user_auth_system.util.JwtUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {

    /** @brief Mocked user repository for testing. */
    @MockitoBean
    private UserRepository userRepository;

    /** @brief Mocked password encoder. */
    @MockitoBean
    private PasswordEncoder passwordEncoder;

    /** @brief Mocked validator for user validation. */
    @MockitoBean
    private Validator validator;

    /** @brief Mocked JWT utility for generating authentication tokens. */
    @MockitoBean
    private JwtUtils jwtUtils;

    /** @brief The user service instance being tested. */
    @Autowired
    private UserService userService;

    /** @brief Sample user data for testing. */
    private UserInfoDTO validUserDTO;

    /** @brief Sample pagination settings. */
    private Pageable pageable;

    /** @brief Sample user 1 for testing. */
    private User user1;

    /** @brief Sample user 2 for testing. */
    private User user2;

    /** @brief Sample login request with valid credentials. */
    private LoginRequestDTO validLoginRequest;

    /** @brief Sample login request with invalid password. */
    private LoginRequestDTO invalidPasswordRequest;

    /** @brief Sample login request with non-existent user. */
    private LoginRequestDTO nonExistentUserRequest;

    /** @brief Sample user update request. */
    private UserUpdateDTO validUpdates;


    /**
     * @brief Initializes test data before each test case.
     */
    @BeforeEach
    public void setup() {
        pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        user1 = new User();
        user1.addRole(new Role(ERole.ROLE_USER));
        user1.setUsername("user1");
        user1.setPassword("password1");
        user1.setEmail("user1@email.com");
        user1.setId(1L);

        user2 = new User();
        user2.addRole(new Role(ERole.ROLE_USER));
        user2.setUsername("user2");
        user2.setPassword("password2");
        user2.setEmail("user2@email.com");
        user2.setId(2L);

        validUserDTO = new UserInfoDTO();
        validUserDTO.setUsername("newUser");
        validUserDTO.setEmail("newuser@example.com");
        validUserDTO.setPassword("password123");

        validLoginRequest = new LoginRequestDTO("user1", "password1");
        invalidPasswordRequest = new LoginRequestDTO("user2", "wrongPassword");
        nonExistentUserRequest = new LoginRequestDTO("unknownUser", "password123");

        validUpdates = new UserUpdateDTO();
        validUpdates.setUsername("newUsername");
        validUpdates.setEmail("new@example.com");
        validUpdates.setPassword("newPassword");
        validUpdates.setRoles(new HashSet<>(Set.of(ERole.ROLE_ADMIN, ERole.ROLE_USER)));
    }

    /**
     * @brief Tests retrieving all users with pagination.
     *
     * Ensures that the user service correctly returns a paginated list of users.
     * Verifies the total number of elements and their correctness.
     */
    @Test
    public void testGettingAllUsers() {
        Page<User> userPage = new PageImpl<>(Arrays.asList(user1, user2));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);

        Page<User> users = userService.findAll(pageable);

        assertNotNull(users);
        assertEquals(2, users.getTotalElements());
        assertEquals("user1", users.getContent().get(0).getUsername());
        assertEquals("user2", users.getContent().get(1).getUsername());
        verify(userRepository, times(1)).findAll(any(Pageable.class));
    }

    /**
     * @brief Tests finding a user by username or email using the username.
     *
     * Ensures that the user service correctly retrieves a user by their username.
     */
    @Test
    public void testFindByUsernameOrEmail_ByUsername() {
        User user1 = new User();
        user1.addRole(new Role(ERole.ROLE_USER));
        user1.setUsername("user1");
        user1.setPassword("password1");
        user1.setEmail("user1@email.com");
        user1.setId(1L);

        when(userRepository.findByUsernameOrEmail("user1", "user1"))
                .thenReturn(Optional.of(user1));

        Optional<User> result = userService.findByUsernameOrEmail("user1");

        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getUsername());
        verify(userRepository, times(1)).findByUsernameOrEmail("user1", "user1");
    }

    /**
     * @brief Tests finding a user by username or email using the email.
     *
     * Ensures that the user service correctly retrieves a user by their email.
     */
    @Test
    public void testFindByUsernameOrEmail_ByEmail() {
        User user1 = new User();
        user1.addRole(new Role(ERole.ROLE_USER));
        user1.setUsername("user1");
        user1.setPassword("password1");
        user1.setEmail("user1@email.com");
        user1.setId(1L);

        when(userRepository.findByUsernameOrEmail("user1@email.com", "user1@email.com"))
                .thenReturn(Optional.of(user1));

        Optional<User> result = userService.findByUsernameOrEmail("user1@email.com");

        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getUsername());
        verify(userRepository, times(1)).findByUsernameOrEmail("user1@email.com", "user1@email.com");
    }

    /**
     * @brief Tests finding a user by username or email when the user is not found.
     *
     * Ensures that the user service returns an empty optional when the user does not exist.
     */
    @Test
    public void testFindByUsernameOrEmail_NotFound() {
        when(userRepository.findByUsernameOrEmail("unknown", "unknown"))
                .thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsernameOrEmail("unknown");

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsernameOrEmail("unknown", "unknown");
    }

    /**
     * @brief Tests deleting a user by ID when the user exists.
     *
     * Ensures that the user service deletes an existing user successfully.
     */
    @Test
    public void testDeleteUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        userService.deleteById(1L);

        verify(userRepository, times(1)).delete(user1);
        verify(userRepository, times(1)).findById(1L);
    }

    /**
     * @brief Tests deleting a user by ID when the user does not exist.
     *
     * Ensures that the user service throws an exception when trying to delete a non-existent user.
     */
    @Test
    public void testDeleteUserById_UserNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            userService.deleteById(2L);
        });

        assertEquals("User not found with id 2", thrownException.getMessage());

        verify(userRepository, never()).delete(any());
        verify(userRepository, times(1)).findById(2L);
    }

    /**
     * @brief Tests retrieving a user by ID when the user exists.
     *
     * Ensures that the user service correctly retrieves a user with a valid ID.
     */
    @Test
    public void testFindById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("user1", result.get().getUsername());

        verify(userRepository, times(1)).findById(1L);
    }

    /**
     * @brief Tests retrieving a user by ID when the user does not exist.
     *
     * Ensures that the user service returns an empty optional when the user is not found.
     */
    @Test
    public void testFindById_UserNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(2L);

        assertFalse(result.isPresent());

        verify(userRepository, times(1)).findById(2L);
    }

    /**
     * @brief Tests registering a user successfully.
     *
     * Ensures that a new user is registered correctly with proper encoding and role assignment.
     */
    @Test
    public void testRegisterUser_Success() {
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(validUserDTO.getUsername());
        savedUser.setEmail(validUserDTO.getEmail());
        savedUser.setPassword("encodedPassword");
        savedUser.addRole(new Role(ERole.ROLE_USER));

        when(validator.validate(validUserDTO)).thenReturn(Collections.emptySet());
        when(passwordEncoder.encode(validUserDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(validUserDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("newUser", result.getUsername());
        assertEquals("newuser@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals("ROLE_USER", result.getRoles().iterator().next().getName().name());

        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * @brief Tests user registration failure due to validation errors.
     *
     * Ensures that an exception is thrown when user registration fails validation.
     */
    @Test
    public void testRegisterUser_ValidationFailure() {
        Set<ConstraintViolation<UserInfoDTO>> violations = Set.of(mock(ConstraintViolation.class));

        when(validator.validate(validUserDTO)).thenReturn(violations);

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            userService.registerUser(validUserDTO);
        });

        assertNotNull(exception);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * @brief Tests registering a user when the username is already taken.
     *
     * Ensures that the user service prevents registration when the username is already in use.
     */
    @Test
    public void testRegisterUser_UsernameAlreadyTaken() {
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername(validUserDTO.getUsername());

        when(validator.validate(validUserDTO)).thenReturn(Collections.emptySet());
        when(userRepository.findByUsername(validUserDTO.getUsername())).thenReturn(Optional.of(existingUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(validUserDTO);
        });

        assertEquals("Username is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * @brief Tests registering a user when the email is already taken.
     *
     * Ensures that the user service prevents registration when the email is already in use.
     */
    @Test
    public void testRegisterUser_EmailAlreadyTaken() {
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail(validUserDTO.getEmail());

        when(validator.validate(validUserDTO)).thenReturn(Collections.emptySet());
        when(userRepository.findByEmail(validUserDTO.getEmail())).thenReturn(Optional.of(existingUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(validUserDTO);
        });

        assertEquals("Email is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * @brief Tests successful user authentication.
     *
     * Ensures that a valid user can log in and receive a JWT token.
     */
    @Test
    public void testAuthenticateUser_Success() {
        when(userRepository.findByUsernameOrEmail("user1", "user1"))
                .thenReturn(Optional.of(user1));

        when(passwordEncoder.matches("password1", user1.getPassword()))
                .thenReturn(true);

        when(jwtUtils.generateJwtToken(String.valueOf(user1.getId()), user1.getUsername()))
                .thenReturn("mocked-jwt-token");

        String token = userService.authenticateUser(validLoginRequest);

        assertNotNull(token);
        assertEquals("mocked-jwt-token", token);

        verify(userRepository, times(1)).findByUsernameOrEmail("user1", "user1");
        verify(passwordEncoder, times(1)).matches("password1", user1.getPassword());
        verify(jwtUtils, times(1)).generateJwtToken(String.valueOf(user1.getId()), user1.getUsername());
    }

    /**
     * @brief Tests user authentication when the user is not found.
     *
     * Ensures that authentication fails when an unknown user attempts to log in.
     */
    @Test
    public void testAuthenticateUser_UserNotFound() {
        when(userRepository.findByUsernameOrEmail("unknownUser", "unknownUser"))
                .thenReturn(Optional.empty());

        String token = userService.authenticateUser(nonExistentUserRequest);

        assertNull(token);

        verify(userRepository, times(1)).findByUsernameOrEmail("unknownUser", "unknownUser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtils, never()).generateJwtToken(anyString(), anyString());
    }

    /**
     * @brief Tests user authentication with an incorrect password.
     *
     * Ensures that authentication fails when the user enters a wrong password.
     */
    @Test
    public void testAuthenticateUser_IncorrectPassword() {
        when(userRepository.findByUsernameOrEmail("user2", "user2"))
                .thenReturn(Optional.of(user2));

        when(passwordEncoder.matches("wrongPassword", user2.getPassword()))
                .thenReturn(false);

        String token = userService.authenticateUser(invalidPasswordRequest);

        assertNull(token);

        verify(userRepository, times(1)).findByUsernameOrEmail("user2", "user2");
        verify(passwordEncoder, times(1)).matches("wrongPassword", user2.getPassword());
        verify(jwtUtils, never()).generateJwtToken(anyString(), anyString());
    }

    /**
     * @brief Tests successful user update.
     *
     * Ensures that the user service correctly updates a user's information.
     */
    @Test
    public void testUpdateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(validator.validate(validUpdates)).thenReturn(Collections.emptySet());
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateUser(validUpdates, 1L);

        assertNotNull(updatedUser);
        assertEquals("newUsername", updatedUser.getUsername());
        assertEquals("new@example.com", updatedUser.getEmail());
        assertEquals("encodedPassword", updatedUser.getPassword());
        assertTrue(updatedUser.getRoles().stream().allMatch(role -> role.getName().name().equals("ROLE_USER") || role.getName().name().equals("ROLE_ADMIN")));

        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * @brief Tests user update with null updates.
     *
     * Ensures that an exception is thrown when trying to update a user with null data.
     */
    @Test
    public void testUpdateUser_NullUpdates() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(null, 1L);
        });

        assertEquals("Updates cannot be null", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * @brief Tests user update when the user is not found.
     *
     * Ensures that an exception is thrown when trying to update a non-existent user.
     */
    @Test
    public void testUpdateUser_UserNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(validUpdates, 2L);
        });

        assertEquals("User not found with id: 2", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * @brief Tests user update when validation fails.
     *
     * Ensures that an exception is thrown when update validation fails.
     */
    @Test
    public void testUpdateUser_ValidationFails() {
        Set<ConstraintViolation<UserUpdateDTO>> violations = Set.of(mock(ConstraintViolation.class));
        when(validator.validate(validUpdates)).thenReturn(violations);

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            userService.updateUser(validUpdates, 1L);
        });

        assertNotNull(exception);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * @brief Tests partial update of a user.
     *
     * Ensures that a user can be partially updated while retaining other attributes.
     */
    @Test
    public void testUpdateUser_PartialUpdate() {
        UserUpdateDTO partialUpdates = new UserUpdateDTO();
        partialUpdates.setUsername("partialUsername");
        partialUpdates.setEmail("partial@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(validator.validate(partialUpdates)).thenReturn(Collections.emptySet());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateUser(partialUpdates, 1L);

        assertNotNull(updatedUser);
        assertEquals("partialUsername", updatedUser.getUsername());
        assertEquals("partial@example.com", updatedUser.getEmail());
        assertEquals("password1", updatedUser.getPassword());
        assertTrue(updatedUser.getRoles().stream().allMatch(role -> role.getName().name().equals("ROLE_USER")));

        verify(userRepository, times(1)).save(any(User.class));
    }

}
