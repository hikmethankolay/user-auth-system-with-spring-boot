/**
 * @file SwaggerConfig.java
 * @brief Configuration class for Swagger/OpenAPI documentation.
 * @details This class configures the OpenAPI documentation for the TÜBİTAK 1001 backend application.
 *          It defines the basic information about the API such as title, description, and version.
 *          The configuration is used by SpringDoc to generate the API documentation that can be
 *          accessed through the Swagger UI.
 */

/**
 * @package com.hikmethankolay.user_auth_system.config
 * @brief Package for configurations in the backend application.
 */
package com.hikmethankolay.user_auth_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @class SwaggerConfig
 * @brief Configuration class for Swagger/OpenAPI documentation.
 * @details Provides bean definitions for configuring the OpenAPI documentation.
 */
@Configuration
public class SwaggerConfig {

    /**
     * @brief Creates and configures an OpenAPI bean for API documentation.
     * @details This bean defines the main properties of the API documentation including
     *          title, description, and version information. It is automatically detected
     *          by SpringDoc to generate the OpenAPI specification.
     *
     * @return A configured OpenAPI instance with the application's API information.
     */
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("User Auth API")
                        .description("User Authentication and Authorization API")
                        .version("v1.0"));
    }
}