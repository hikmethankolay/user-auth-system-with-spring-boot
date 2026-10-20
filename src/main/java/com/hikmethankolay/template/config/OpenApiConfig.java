package com.hikmethankolay.template.config;

import com.hikmethankolay.template.common.ApiDocs;
import com.hikmethankolay.template.common.TraceIdResponseFilter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * OpenAPI 3 document served at {@code /v3/api-docs} and rendered by Swagger UI at {@code /swagger-ui.html}.
 *
 * <p>springdoc builds the document from the controllers; this class adds what it cannot infer:</p>
 * <ul>
 *   <li>the RFC 9457 {@code Problem} schema and the error responses every operation can return
 *       (400 for operations with input, 404 for operations on {@code {id}}, 429 everywhere);</li>
 *   <li>the {@code X-Trace-Id} and {@code RateLimit-*} headers on every response and {@code Retry-After} on 429;</li>
 *   <li>the page size limit ({@code spring.data.web.pageable.max-page-size}) on {@code size} parameters;</li>
 *   <li>a relative server URL, so Swagger UI and generated clients call the host that serves the document.</li>
 * </ul>
 *
 * <p>Security schemes, public endpoints and 401/403 responses are added by
 * {@code com.hikmethankolay.template.security.ApiDocsSecurityCustomizer}.</p>
 */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    /** Component name of the problem detail schema. */
    static final String PROBLEM = "Problem";

    /** Component name of a field-level validation error. */
    static final String FIELD_VIOLATION = "FieldViolation";

    /** Response headers present on every API response, keyed by name. */
    private static final Map<String, String> COMMON_HEADERS = Map.of(
            TraceIdResponseFilter.HEADER, "Trace id of the request; quote it when reporting an error",
            "RateLimit-Limit", "Requests allowed per minute for the client IP",
            "RateLimit-Remaining", "Requests left in the current window",
            "RateLimit-Reset", "Seconds until the window resets");

    /**
     * Describes the API.
     *
     * @param applicationName value of {@code spring.application.name}, used as the document title
     * @return the OpenAPI model that springdoc merges with the scanned endpoints
     */
    @Bean
    OpenAPI openApi(@Value("${spring.application.name}") String applicationName) {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName)
                        .version("v1")
                        .description("REST API. Errors are RFC 9457 problem details (application/problem+json).")
                        .license(new License().name("MIT").url("https://opensource.org/license/mit")))
                .servers(List.of(new Server().url("/").description("The host serving this document")))
                .components(new Components()
                        .addSchemas(PROBLEM, problemSchema())
                        .addSchemas(FIELD_VIOLATION, fieldViolationSchema()));
    }

    /**
     * Adds the shared error responses, response headers and the page size limit to every operation.
     * Runs last, so responses added by other customizers get the headers too.
     *
     * @param maxPageSize value of {@code spring.data.web.pageable.max-page-size}
     * @return the customizer springdoc applies after scanning the controllers
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    OpenApiCustomizer errorResponsesAndHeaders(@Value("${spring.data.web.pageable.max-page-size:2000}") int maxPageSize) {
        return openApi -> openApi.getPaths().forEach((path, item) -> item.readOperations().forEach(operation -> {
            ApiResponses responses = operation.getResponses();
            if (hasInput(operation)) {
                responses.putIfAbsent("400", problem("Invalid input; validation errors are listed in `errors`"));
            }
            if (path.contains("{id}")) {
                responses.putIfAbsent("404", problem("No user with this id"));
            }
            responses.putIfAbsent("429", problem("Too many requests or login attempts; wait for `Retry-After` seconds")
                    .addHeaderObject("Retry-After", header("Seconds to wait before retrying", new IntegerSchema())));
            responses.values().forEach(OpenApiConfig::addCommonHeaders);
            if (operation.getParameters() != null) {
                operation.getParameters().stream()
                        .filter(parameter -> "size".equals(parameter.getName()) && parameter.getSchema() != null)
                        .forEach(parameter -> parameter.getSchema().setMaximum(BigDecimal.valueOf(maxPageSize)));
            }
        }));
    }

    /**
     * Builds an error response with the problem detail body.
     *
     * @param description when the error occurs
     * @return the response object
     */
    public static ApiResponse problem(String description) {
        return new ApiResponse().description(description).content(new Content()
                .addMediaType(ApiDocs.PROBLEM_JSON, new MediaType().schema(new Schema<>().$ref(ApiDocs.PROBLEM_SCHEMA))));
    }

    /**
     * Tells whether an operation takes a body or parameters, and can therefore be rejected with 400.
     *
     * @param operation the operation
     * @return {@code true} if it has a request body or parameters
     */
    private static boolean hasInput(Operation operation) {
        return operation.getRequestBody() != null
                || (operation.getParameters() != null && !operation.getParameters().isEmpty());
    }

    /**
     * Adds the trace id and rate limit headers to a response, keeping headers it already declares.
     *
     * @param response the response to extend
     */
    private static void addCommonHeaders(ApiResponse response) {
        COMMON_HEADERS.forEach((name, description) -> {
            if (response.getHeaders() == null || !response.getHeaders().containsKey(name)) {
                Schema<?> schema = name.startsWith("RateLimit") ? new IntegerSchema() : new StringSchema();
                response.addHeaderObject(name, header(description, schema));
            }
        });
    }

    /**
     * Builds a response header description.
     *
     * @param description what the header carries
     * @param schema      the header's type
     * @return the header object
     */
    private static Header header(String description, Schema<?> schema) {
        return new Header().description(description).schema(schema);
    }

    /**
     * The RFC 9457 problem detail produced by {@code GlobalExceptionHandler} and the security error handlers.
     *
     * @return the schema
     */
    private static Schema<?> problemSchema() {
        return new ObjectSchema()
                .description("RFC 9457 problem detail")
                .addProperty("type", new StringSchema().format("uri").example("about:blank"))
                .addProperty("title", new StringSchema().example("Bad Request"))
                .addProperty("status", new IntegerSchema().example(400))
                .addProperty("detail", new StringSchema().example("Request validation failed."))
                .addProperty("instance", new StringSchema().format("uri-reference").example("/api/v1/auth/register"))
                .addProperty("errors", new ArraySchema()
                        .items(new Schema<>().$ref("#/components/schemas/" + FIELD_VIOLATION))
                        .description("Field errors; only on validation failures"))
                .required(List.of("type", "title", "status"));
    }

    /**
     * One entry of {@code errors} in a validation problem.
     *
     * @return the schema
     */
    private static Schema<?> fieldViolationSchema() {
        return new ObjectSchema()
                .addProperty("field", new StringSchema().example("email"))
                .addProperty("message", new StringSchema().example("must be a well-formed email address"))
                .required(List.of("field", "message"));
    }
}
