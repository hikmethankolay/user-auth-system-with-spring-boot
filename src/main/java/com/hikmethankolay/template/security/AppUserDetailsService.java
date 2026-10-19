package com.hikmethankolay.template.security;

import com.hikmethankolay.template.user.UserRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads users for password authentication. Either the username or the email can be used to log in.
 */
@Service
class AppUserDetailsService implements UserDetailsService {

    /** User persistence. */
    private final UserRepository userRepository;

    /**
     * Creates the service.
     *
     * @param userRepository user persistence
     */
    AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Finds a user by username or (case-insensitive) email.
     *
     * @param identifier username or email entered at login
     * @return the user's security view
     * @throws UsernameNotFoundException if no user matches
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) {
        return userRepository.findByUsernameOrEmail(identifier, identifier.toLowerCase(Locale.ROOT))
                .map(AppUserDetails::from)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
