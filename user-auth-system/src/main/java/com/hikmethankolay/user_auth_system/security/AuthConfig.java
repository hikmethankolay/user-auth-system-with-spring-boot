/**
 * @file AuthConfig.java
 * @brief Security configuration for authentication.
 *
 * This class defines beans for password encoding and authentication management.
 *
 * @author Hikmethan Kolay
 * @date 2025-02-12
 */

/**
 * @package com.hikmethankolay.user_auth_system.security
 * @brief Contains the core components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @class AuthConfig
 * @brief Security configuration class for authentication management.
 */
@Configuration
public class AuthConfig {

    /**
     * @brief Bean definition for password encoding.
     * @return A BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * @brief Bean definition for authentication manager.
     * @param authenticationConfiguration The authentication configuration.
     * @return The authentication manager instance.
     * @throws Exception if an error occurs during retrieval.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}