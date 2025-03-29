/**
 * @file SwaggerConfigTest.java
 * @brief Tests for the SwaggerConfig class.
 *
 * Contains unit tests for OpenAPI documentation configuration.
 *
 * @author Test Suite Generator
 * @date 2025-03-29
 */
package com.hikmethankolay.user_auth_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @class SwaggerConfigTest
 * @brief Test class for SwaggerConfig.
 *
 * This class contains unit tests for OpenAPI configuration.
 */
@SpringBootTest
public class SwaggerConfigTest {

    /**
     * SwaggerConfig instance to be tested.
     */
    @InjectMocks
    private SwaggerConfig swaggerConfig;

    /**
     * @brief Test OpenAPI bean configuration.
     *
     * Verifies that springShopOpenAPI correctly configures OpenAPI information.
     */
    @Test
    public void testSpringShopOpenAPI() {
        // Act
        OpenAPI openAPI = swaggerConfig.springShopOpenAPI();
        
        // Assert
        assertNotNull(openAPI);
        
        // Verify Info object
        Info info = openAPI.getInfo();
        assertNotNull(info);
        
        // Verify info properties
        assertEquals("User Auth API", info.getTitle());
        assertEquals("User Authentication and Authorization API", info.getDescription());
        assertEquals("v1.0", info.getVersion());
    }
}
