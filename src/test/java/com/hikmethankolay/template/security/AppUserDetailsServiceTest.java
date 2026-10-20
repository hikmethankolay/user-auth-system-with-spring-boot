package com.hikmethankolay.template.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hikmethankolay.template.user.Role;
import com.hikmethankolay.template.user.User;
import com.hikmethankolay.template.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

/** Unit tests of {@link AppUserDetailsService} and {@link AppUserDetails}. */
class AppUserDetailsServiceTest {

    /** Reads the users' session versions. */
    private final UserRepository users = mock(UserRepository.class);
    /** The service under test. */
    private final AppUserDetailsService service = new AppUserDetailsService(users);

    /** Users are found by username or lower-cased email, and their security view never prints the hash. */
    @Test
    void findsUsersByUsernameOrLowerCaseEmail() {
        User user = new User("alice", "alice@example.com", "{bcrypt}hash", Role.ADMIN);
        ReflectionTestUtils.setField(user, "id", 5L);
        when(users.findByUsernameOrEmail("Alice@Example.com", "alice@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("Alice@Example.com");

        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getPassword()).isEqualTo("{bcrypt}hash");
        assertThat(details.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_ADMIN");
        // Safe to log: no password hash.
        assertThat(details.toString())
                .isEqualTo("AppUserDetails[id=5, username=alice, role=ADMIN]")
                .doesNotContain("hash");
    }

    /** An unknown identifier is reported as not found. */
    @Test
    void unknownUserIsReportedAsNotFound() {
        when(users.findByUsernameOrEmail("nobody", "nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("nobody")).isInstanceOf(UsernameNotFoundException.class);
    }
}
