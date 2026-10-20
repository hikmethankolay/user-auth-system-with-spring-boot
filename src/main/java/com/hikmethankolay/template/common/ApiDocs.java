package com.hikmethankolay.template.common;

/**
 * Constants for OpenAPI annotations on controllers, so every error response points to the same
 * problem detail schema (registered by {@code OpenApiConfig}).
 */
public final class ApiDocs {

    /** Media type of every error response (RFC 9457). */
    public static final String PROBLEM_JSON = "application/problem+json";

    /** Reference to the problem detail schema in the OpenAPI components. */
    public static final String PROBLEM_SCHEMA = "#/components/schemas/Problem";

    /** Not instantiable. */
    private ApiDocs() {
    }
}
