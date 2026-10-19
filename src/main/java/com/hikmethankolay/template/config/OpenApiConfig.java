package com.hikmethankolay.template.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 document served at {@code /v3/api-docs} and rendered by Swagger UI at {@code /swagger-ui.html}.
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    /** Name of the bearer-token security scheme referenced by every operation. */
    private static final String BEARER_SCHEME = "bearerAuth";

    /**
     * Describes the API and declares JWT bearer authentication, so Swagger UI offers an "Authorize" button.
     *
     * @param applicationName value of {@code spring.application.name}, used as the document title
     * @return the OpenAPI model that springdoc merges with the scanned endpoints
     */
    @Bean
    OpenAPI openApi(@Value("${spring.application.name}") String applicationName) {
        return new OpenAPI()
                .info(new Info().title(applicationName).version("v1"))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
