package com.hikmethankolay.template;

import static org.assertj.core.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Checks every response of the integration tests against the committed {@code api/openapi.json}: the
 * status must be documented for the operation, and the body must have a documented media type and match
 * its schema (or be empty when none is documented). Requests the document does not describe (actuator,
 * Swagger UI, methods a path does not support) are not checked.
 *
 * <p>{@code OpenApiSpecExportTest} keeps the file equal to the generated document, so together they prove
 * that the document describes what the API really returns.</p>
 */
final class OpenApiConformance implements ResultMatcher {

    /** The committed document, relative to the project directory Maven runs the tests in. */
    private static final Path SPEC = Path.of("api", "openapi.json");

    /** Shared instance; the document is read once per test JVM. */
    private static final OpenApiConformance INSTANCE = new OpenApiConformance();

    private final JsonNode spec;
    private final ObjectMapper json = new ObjectMapper();
    private final JsonSchemaFactory schemas = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    private final Map<String, JsonSchema> compiled = new ConcurrentHashMap<>();

    /** Documented paths and their patterns; paths without variables first, so {@code /users/me} wins over {@code /users/{id}}. */
    private final List<Map.Entry<String, Pattern>> paths = new ArrayList<>();

    private OpenApiConformance() {
        try {
            spec = json.readTree(Files.readString(SPEC));
        } catch (IOException ex) {
            throw new UncheckedIOException("Cannot read " + SPEC.toAbsolutePath(), ex);
        }
        spec.path("paths")
                .fieldNames()
                .forEachRemaining(path -> paths.add(Map.entry(
                        path, Pattern.compile(Pattern.quote(path).replaceAll("\\{[^}]+}", "\\\\E[^/]+\\\\Q")))));
        paths.sort(Comparator.comparing(entry -> entry.getKey().contains("{")));
    }

    /**
     * Returns the matcher, for {@code alwaysExpect}.
     *
     * @return the shared instance
     */
    static OpenApiConformance instance() {
        return INSTANCE;
    }

    @Override
    public void match(MvcResult result) throws Exception {
        MockHttpServletRequest request = result.getRequest();
        MockHttpServletResponse response = result.getResponse();
        String path = documentedPath(request);
        JsonNode operation = path == null
                ? null
                : spec.path("paths").path(path).path(request.getMethod().toLowerCase(Locale.ROOT));
        if (operation == null || operation.isMissingNode()) {
            return;
        }
        String where = request.getMethod() + " " + path + " -> " + response.getStatus();
        JsonNode documented = resolve(operation.path("responses").path(Integer.toString(response.getStatus())));
        if (documented.isMissingNode()) {
            fail(where + ": status not documented in " + SPEC + "; body: " + response.getContentAsString());
        }

        JsonNode content = documented.path("content");
        if (content.isEmpty()) {
            if (response.getContentLength() > 0 || response.getContentAsByteArray().length > 0) {
                fail(where + ": documented without a body, but returned " + response.getContentAsString());
            }
            return;
        }
        if (response.getContentType() == null) {
            fail(where + ": documented with a body, but returned none");
        }
        MediaType actual = MediaType.parseMediaType(response.getContentType());
        String mediaType = null;
        for (String candidate : (Iterable<String>) content::fieldNames) {
            if (MediaType.parseMediaType(candidate).equalsTypeAndSubtype(actual)) {
                mediaType = candidate;
            }
        }
        if (mediaType == null) {
            fail(where + ": content type " + actual + " not documented; documented: " + content.properties());
        }
        JsonNode schema = content.path(mediaType).path("schema");
        Set<ValidationMessage> errors = compiled.computeIfAbsent(where + " " + mediaType, key -> compile(schema))
                .validate(json.readTree(response.getContentAsByteArray()));
        if (!errors.isEmpty()) {
            fail(where + ": body does not match the documented schema: " + errors + "; body: "
                    + response.getContentAsString());
        }
    }

    /** The document path of the request: the handler's pattern, or the matching template for filter responses. */
    private String documentedPath(MockHttpServletRequest request) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern != null && spec.path("paths").has(pattern.toString())) {
            return pattern.toString();
        }
        return paths.stream()
                .filter(entry ->
                        entry.getValue().matcher(request.getRequestURI()).matches())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /** Follows a {@code $ref} to a shared response. */
    private JsonNode resolve(JsonNode response) {
        return response.has("$ref") ? spec.at(response.get("$ref").asText().substring(1)) : response;
    }

    /** Compiles a media type schema; its {@code #/components/...} references resolve against the document's components. */
    private JsonSchema compile(JsonNode schema) {
        ObjectNode root = schema.deepCopy();
        root.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        root.set("components", spec.path("components"));
        return schemas.getSchema(root);
    }

    /** Applies the check to every MockMvc request of the integration tests. */
    @TestConfiguration(proxyBeanMethods = false)
    static class Configuration {

        @Bean
        MockMvcBuilderCustomizer openApiConformance() {
            return builder -> builder.alwaysExpect(instance());
        }
    }
}
