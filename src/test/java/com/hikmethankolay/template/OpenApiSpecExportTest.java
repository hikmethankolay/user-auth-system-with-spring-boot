package com.hikmethankolay.template;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Keeps the committed OpenAPI document ({@code api/openapi.json}) identical to the one the application
 * generates, so every API change shows up as a reviewable diff of that file.
 *
 * <p>When the documents differ, the test fails. After an intended API change, update the committed file
 * with {@code scripts/test.sh -Dtest=OpenApiSpecExportTest -Dopenapi.update=true} and commit it with the
 * code. The generated document is also written to {@code target/openapi.json}, which
 * {@code scripts/site.sh} publishes as the REST API reference.</p>
 */
class OpenApiSpecExportTest extends IntegrationTest {

    private static final Path COMMITTED = Path.of("api", "openapi.json");
    private static final Path GENERATED = Path.of("target", "openapi.json");
    private static final JsonMapper JSON = JsonMapper.builder().enable(SerializationFeature.INDENT_OUTPUT).build();

    @Test
    void committedSpecMatchesTheGeneratedOne() throws Exception {
        String spec = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode generated = JSON.readTree(spec);
        String pretty = JSON.writeValueAsString(generated) + "\n";

        Files.createDirectories(GENERATED.getParent());
        Files.writeString(GENERATED, pretty, StandardCharsets.UTF_8);

        if (Boolean.getBoolean("openapi.update") || !Files.exists(COMMITTED)) {
            Files.createDirectories(COMMITTED.getParent());
            Files.writeString(COMMITTED, pretty, StandardCharsets.UTF_8);
            return;
        }
        assertThat(JSON.readTree(Files.readString(COMMITTED, StandardCharsets.UTF_8)))
                .as("api/openapi.json is out of date. If the API change is intended, run "
                        + "scripts/test.sh -Dtest=OpenApiSpecExportTest -Dopenapi.update=true and commit the file. "
                        + "Compare with: diff api/openapi.json target/openapi.json")
                .isEqualTo(generated);
    }
}
