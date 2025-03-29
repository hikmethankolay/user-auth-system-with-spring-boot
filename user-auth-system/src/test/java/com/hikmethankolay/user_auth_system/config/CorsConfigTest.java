/**
 * @file CorsConfigTest.java
 * @brief Tests for the CorsConfig class.
 *
 * Contains unit tests for CORS configuration.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @class CorsConfigTest
 * @brief Test class for CorsConfig.
 *
 * This class contains unit tests for CORS configuration.
 */
@SpringBootTest
public class CorsConfigTest {

    /**
     * CorsConfig instance to be tested.
     */
    @InjectMocks
    private CorsConfig corsConfig;

    /**
     * @brief Test CORS configuration source bean.
     *
     * Verifies that corsConfigurationSource correctly configures CORS settings.
     */
    @Test
    public void testCorsConfigurationSource() {
        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();

        // Assert - verify source is the correct type
        assertNotNull(source);
        assertInstanceOf(UrlBasedCorsConfigurationSource.class, source);

        // Create a mock HttpServletRequest
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users");

        // Get the configuration to verify settings
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlSource.getCorsConfiguration(request);

        // Assert configuration values
        assertNotNull(config);

        // Check allowed origins
        List<String> expectedOrigins = Arrays.asList("http://localhost:3000", "https://yourdomain.com");
        assertEquals(expectedOrigins, config.getAllowedOrigins());

        // Check allowed methods
        List<String> expectedMethods = Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertTrue(config.getAllowedMethods().containsAll(expectedMethods));

        // Check allowed headers
        List<String> expectedHeaders = Arrays.asList("Authorization", "Content-Type");
        assertTrue(config.getAllowedHeaders().containsAll(expectedHeaders));

        // Check credentials allowed
        assertEquals(Boolean.TRUE, config.getAllowCredentials());
    }

    /**
     * @brief Test CORS configuration for all paths.
     *
     * Verifies that CORS configuration is applied to all URL paths.
     */
    @Test
    public void testCorsConfigurationForAllPaths() {
        // Act
        CorsConfigurationSource source = corsConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;

        // Assert - test various paths to ensure configuration is applied
        String[] testPaths = {
                "/api/auth/login",
                "/api/users",
                "/api/roles",
                "/some/random/path"
        };

        for (String path : testPaths) {
            // Create a mock HttpServletRequest with the test path
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI(path);

            // Get configuration using the request instead of the string path
            CorsConfiguration config = urlSource.getCorsConfiguration(request);
            assertNotNull(config, "CORS configuration should be applied to path: " + path);
            assertTrue(config.getAllowedOrigins().contains("http://localhost:3000"));
        }
    }
}