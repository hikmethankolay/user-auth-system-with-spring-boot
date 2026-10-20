package com.hikmethankolay.template.common;

import java.net.URI;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

/**
 * The {@code type} of the problem details this API returns, so clients can tell errors apart without
 * parsing {@code detail}. Each type is the URN {@code urn:problem:<code>}; errors raised by Spring MVC
 * itself (malformed JSON, unsupported media type, ...) keep {@code about:blank}.
 *
 * <p>The list is published in the OpenAPI document, in the description of {@code Problem.type}.</p>
 */
public enum ProblemType {

    /** The request body failed Bean Validation; the fields are listed in {@code errors}. */
    VALIDATION_FAILED("validation-failed", "The request body is invalid; the fields are listed in `errors`"),

    /** The {@code sort} parameter names a property that cannot be sorted by. */
    INVALID_SORT("invalid-sort", "The `sort` parameter names a property that cannot be sorted by"),

    /** Wrong login credentials, or a refresh token that is missing, invalid, expired or already used. */
    INVALID_CREDENTIALS(
            "invalid-credentials",
            "Wrong username, email or password, or a refresh token that is missing, invalid, expired or already used"),

    /** The access token is missing, invalid, expired or revoked. */
    UNAUTHENTICATED("unauthenticated", "The access token is missing, invalid, expired or revoked"),

    /** The caller is authenticated but lacks the required role. */
    FORBIDDEN("forbidden", "The caller is authenticated but lacks the required role"),

    /** Login refused because the email address is not verified yet. */
    EMAIL_NOT_VERIFIED("email-not-verified", "The email address must be verified before logging in"),

    /** The caller's current password, required to confirm a change, is missing or wrong. */
    WRONG_PASSWORD(
            "wrong-password", "The caller's current password, required to confirm the change, is missing or wrong"),

    /** An email verification or password reset token is unknown, used or expired. */
    INVALID_TOKEN("invalid-token", "The email verification or password reset token is unknown, used or expired"),

    /** The username belongs to another account. */
    USERNAME_TAKEN("username-taken", "The username belongs to another account"),

    /** The email address belongs to another account. */
    EMAIL_TAKEN("email-taken", "The email address belongs to another account"),

    /** Only the owner can change roles and manage administrators. */
    OWNER_REQUIRED("owner-required", "Only the owner can change roles and manage administrators"),

    /** The owner account cannot be deleted or demoted, and the OWNER role cannot be assigned, through the API. */
    OWNER_PROTECTED(
            "owner-protected",
            "The owner account cannot be deleted or demoted, and the OWNER role cannot be assigned, through the API"),

    /** An administrator tried to delete their own account through the admin endpoint. */
    OWN_ACCOUNT("own-account", "Administrators cannot delete their own account through the admin endpoint"),

    /** No user has the requested id. */
    USER_NOT_FOUND("user-not-found", "No user has this id"),

    /** The change conflicts with data written concurrently. */
    CONFLICT("conflict", "The change conflicts with data written at the same time; retry it"),

    /** Too many failed logins for the IP or the account. */
    LOGIN_LOCKED("login-locked", "Too many failed logins for this IP or account; wait for `Retry-After` seconds"),

    /** The client exceeded the request rate limit. */
    RATE_LIMITED("rate-limited", "Too many requests from this IP; wait for `Retry-After` seconds"),

    /** An unexpected server error. */
    INTERNAL_ERROR("internal-error", "Unexpected server error; quote `X-Trace-Id` when reporting it");

    private static final String URN_PREFIX = "urn:problem:";

    private final URI uri;

    private final String description;

    /**
     * Creates a problem type.
     *
     * @param code        the last part of the URN
     * @param description when the problem occurs
     */
    ProblemType(String code, String description) {
        this.uri = URI.create(URN_PREFIX + code);
        this.description = description;
    }

    /**
     * Returns the value of the {@code type} member.
     *
     * @return the URN, e.g. {@code urn:problem:username-taken}
     */
    public URI uri() {
        return uri;
    }

    /**
     * Returns when the problem occurs.
     *
     * @return a one-line description
     */
    public String description() {
        return description;
    }

    /**
     * Builds a problem detail of this type.
     *
     * @param status the response status
     * @param detail human-readable explanation, sent to the client
     * @return the problem detail
     */
    public ProblemDetail problem(HttpStatusCode status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(uri);
        return problem;
    }
}
