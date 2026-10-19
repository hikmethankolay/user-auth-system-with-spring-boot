package com.hikmethankolay.template.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hikmethankolay.template.audit.AuditLog;
import com.hikmethankolay.template.common.ApiException;
import com.hikmethankolay.template.user.dto.AdminUpdateUserRequest;
import com.hikmethankolay.template.user.dto.ChangePasswordRequest;
import com.hikmethankolay.template.user.dto.DeleteAccountRequest;
import com.hikmethankolay.template.user.dto.UserResponse;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher events;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, events, new AuditLog(new SimpleMeterRegistry()));
    }

    @Test
    void registerNormalizesEmailAndHashesPassword() {
        when(passwordEncoder.encode("Secret#123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 1L));

        UserResponse response = userService.register("alice", "  Alice@Example.COM ", "Secret#123", Role.USER, false);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("hashed");
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(response.emailVerified()).isFalse();
        verify(events).publishEvent(new EmailVerificationRequired(1L));
    }

    @Test
    void registerVerifiedUserSendsNoVerification() {
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> withId(invocation.getArgument(0), 2L));

        UserResponse response = userService.register("root", "root@example.com", "Secret#123", Role.ADMIN, true);

        assertThat(response.emailVerified()).isTrue();
        verify(events, never()).publishEvent(any());
    }

    @Test
    void registerRejectsTakenEmail() {
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("alice", "alice@example.com", "Secret#123", Role.USER, false))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        User user = withId(new User("alice", "alice@example.com", "hashed", Role.USER), 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword(1L, new ChangePasswordRequest("wrong", "New#12345")))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThat(user.getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void adminUpdateLeavesNullFieldsUnchanged() {
        User user = withId(new User("alice", "alice@example.com", "hashed", Role.USER), 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);

        UserResponse response = userService.update(1L, new AdminUpdateUserRequest(null, null, null, Role.ADMIN));

        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.role()).isEqualTo(Role.ADMIN);
        assertThat(user.getPasswordHash()).isEqualTo("hashed");
    }

    @Test
    void deleteRejectsOwnAccount() {
        assertThatThrownBy(() -> userService.delete(5L, 5L))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void getByIdThrowsNotFoundForUnknownUser() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(9L))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void lastAdministratorCannotDeleteTheirAccount() {
        User admin = withId(new User("root", "root@example.com", "hashed", Role.ADMIN), 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("Secret#123", "hashed")).thenReturn(true);
        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> userService.deleteOwnAccount(1L, new DeleteAccountRequest("Secret#123")))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deletingOwnAccountEndsItsSessions() {
        User user = withId(new User("alice", "alice@example.com", "hashed", Role.USER), 5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret#123", "hashed")).thenReturn(true);

        userService.deleteOwnAccount(5L, new DeleteAccountRequest("Secret#123"));

        verify(userRepository).delete(user);
        verify(events).publishEvent(new SessionsInvalidated(5L));
    }

    private static User withId(User user, long id) {
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
