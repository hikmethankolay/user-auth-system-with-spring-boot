package com.hikmethankolay.template.common;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Renders every error as an RFC 9457 problem detail ({@code application/problem+json}).
 *
 * <p>Example response:</p>
 * <pre>
 * HTTP/1.1 400 Bad Request
 * Content-Type: application/problem+json
 *
 * {"type":"urn:problem:validation-failed","title":"Bad Request","status":400,"detail":"Request validation failed.",
 *  "instance":"/api/v1/auth/register","errors":[{"field":"email","message":"must be a well-formed email address"}]}
 * </pre>
 *
 * <p>Spring MVC exceptions and {@link ApiException} are handled by the base class. Errors raised
 * in the security filter chain are routed here as well (see {@code SecurityProblemHandler}).</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** Logger for unexpected and data-integrity errors. */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maps missing, invalid or expired credentials to {@code 401 Unauthorized}.
     *
     * @param ex the authentication failure
     * @return a 401 problem with a {@code WWW-Authenticate: Bearer} header
     */
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex) {
        ProblemDetail problem = ex instanceof BadCredentialsException
                ? ProblemType.INVALID_CREDENTIALS.problem(HttpStatus.UNAUTHORIZED, ex.getMessage())
                : ProblemType.UNAUTHENTICATED.problem(HttpStatus.UNAUTHORIZED,
                        "Authentication is required to access this resource.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .body(problem);
    }

    /**
     * Maps authenticated-but-not-allowed requests to {@code 403 Forbidden}.
     *
     * @param ex the authorization failure
     * @return a 403 problem
     */
    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return ProblemType.FORBIDDEN.problem(HttpStatus.FORBIDDEN, "You do not have permission to access this resource.");
    }

    /**
     * Maps database constraint violations (e.g. a unique key race) and writes based on a stale read
     * (optimistic locking) to {@code 409 Conflict}.
     *
     * @param ex the violation; its details are logged, never sent to the client
     * @return a 409 problem
     */
    @ExceptionHandler({DataIntegrityViolationException.class, OptimisticLockingFailureException.class})
    ProblemDetail handleDataIntegrity(DataAccessException ex) {
        log.warn("Conflicting write: {}", ex.getMostSpecificCause().getMessage());
        return ProblemType.CONFLICT.problem(HttpStatus.CONFLICT, "The request conflicts with existing data.");
    }

    /**
     * Maps an unknown {@code sort} property in a paginated request to {@code 400 Bad Request}.
     *
     * @param ex the unresolved property reference
     * @return a 400 problem naming the property
     */
    @ExceptionHandler(PropertyReferenceException.class)
    ProblemDetail handleInvalidSortProperty(PropertyReferenceException ex) {
        return ProblemType.INVALID_SORT.problem(HttpStatus.BAD_REQUEST, "Unknown sort property: " + ex.getPropertyName());
    }

    /**
     * Catch-all for unexpected errors: logs the stack trace and returns a generic {@code 500}.
     *
     * @param ex the unexpected exception
     * @return a 500 problem that does not leak internal details
     */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ProblemType.INTERNAL_ERROR.problem(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    /**
     * Adds field-level details to Bean Validation failures of {@code @Valid} request bodies.
     *
     * @param ex      the validation failure
     * @param headers headers prepared by the base class
     * @param status  the response status (400)
     * @param request the current request
     * @return a 400 problem with an {@code errors} array of {@link FieldViolation}s
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldViolation> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
                .toList();

        ProblemDetail body = ex.getBody();
        body.setType(ProblemType.VALIDATION_FAILED.uri());
        body.setDetail("Request validation failed.");
        body.setProperty("errors", errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    /**
     * One invalid field of a request body.
     *
     * @param field   JSON property name
     * @param message why the value was rejected
     */
    public record FieldViolation(String field, String message) {
    }
}
