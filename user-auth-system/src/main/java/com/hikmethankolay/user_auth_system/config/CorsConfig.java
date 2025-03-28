/**
 * @file CorsConfig.java
 * @brief Cross-Origin Resource Sharing (CORS) configuration.
 *
 * This class configures CORS policies to allow controlled access to API resources
 * from different origins, which is essential for frontend-backend communication.
 *
 * @author Hikmethan Kolay
 * @date 2025-03-29
 */

/**
 * @package com.hikmethankolay.user_auth_system.config
 * @brief Contains configuration components of the User Authentication System.
 */
package com.hikmethankolay.user_auth_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * @class CorsConfig
 * @brief Configuration for Cross-Origin Resource Sharing (CORS).
 *
 * This configuration allows the API to accept requests from specified origins,
 * with defined methods and headers, which is necessary for browser-based clients.
 */
@Configuration
public class CorsConfig {

    /**
     * @brief Creates and configures a CORS configuration source.
     * @return A fully configured CorsConfigurationSource bean.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Define allowed origins for cross-origin requests
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://yourdomain.com"));

        // Define allowed HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Define allowed request headers
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Allow cookies and authentication headers
        configuration.setAllowCredentials(true);

        // Apply this configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}