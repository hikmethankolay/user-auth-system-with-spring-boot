package com.hikmethankolay.template.common;

import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Domain error that is rendered as an RFC 9457 {@code application/problem+json} response.
 *
 * <p>Throw it from services through the static factories; {@link GlobalExceptionHandler}
 * turns it into a response with the given status, detail message and headers.</p>
 */
public class ApiException extends ErrorResponseException {

    /**
     * Creates an exception for the given status.
     *
     * @param status HTTP status of the response
     * @param detail human-readable explanation, sent to the client as {@code detail}
     */
    public ApiException(HttpStatus status, String detail) {
        super(status, ProblemDetail.forStatusAndDetail(status, detail), null);
    }

    /**
     * Creates a {@code 400 Bad Request} error.
     *
     * @param detail explanation sent to the client
     * @return the exception to throw
     */
    public static ApiException badRequest(String detail) {
        return new ApiException(HttpStatus.BAD_REQUEST, detail);
    }

    /**
     * Creates a {@code 404 Not Found} error.
     *
     * @param detail explanation sent to the client
     * @return the exception to throw
     */
    public static ApiException notFound(String detail) {
        return new ApiException(HttpStatus.NOT_FOUND, detail);
    }

    /**
     * Creates a {@code 409 Conflict} error, e.g. for a duplicate username.
     *
     * @param detail explanation sent to the client
     * @return the exception to throw
     */
    public static ApiException conflict(String detail) {
        return new ApiException(HttpStatus.CONFLICT, detail);
    }

    /**
     * Creates a {@code 429 Too Many Requests} error with a {@code Retry-After} header.
     *
     * @param detail     explanation sent to the client
     * @param retryAfter how long the client should wait before retrying
     * @return the exception to throw
     */
    public static ApiException tooManyRequests(String detail, Duration retryAfter) {
        ApiException exception = new ApiException(HttpStatus.TOO_MANY_REQUESTS, detail);
        exception.getHeaders().set(HttpHeaders.RETRY_AFTER, Long.toString(retryAfter.toSeconds()));
        return exception;
    }
}
