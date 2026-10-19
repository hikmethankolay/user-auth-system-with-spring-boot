package com.hikmethankolay.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Exports the OpenAPI document of the running application to {@code target/openapi.json}.
 * {@code scripts/site.sh} publishes it as the REST API reference of the documentation site.
 */
class OpenApiSpecExportTest extends IntegrationTest {

    private static final Path OUTPUT = Path.of("target", "openapi.json");

    @Test
    void exportsOpenApiDocument() throws Exception {
        String spec = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(spec).contains("\"openapi\"", "/api/v1/auth/login", "/api/v1/users/me", "/api/v1/auth/password-reset");
        Files.createDirectories(OUTPUT.getParent());
        Files.writeString(OUTPUT, spec, StandardCharsets.UTF_8);
    }
}
