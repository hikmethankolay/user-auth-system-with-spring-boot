package com.hikmethankolay.template.common;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Adds the current trace id to every response as the {@code X-Trace-Id} header.
 *
 * <p>The same id appears in every log line of the request, so a client reporting an error can
 * quote it and the matching logs and trace can be found directly. Runs right after Spring's
 * server observation filter, which starts the trace.</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class TraceIdResponseFilter extends OncePerRequestFilter {

    /** Response header carrying the trace id. */
    public static final String HEADER = "X-Trace-Id";

    /** The tracer; absent when tracing is disabled, e.g. in most tests. */
    private final ObjectProvider<Tracer> tracer;

    /**
     * Creates the filter.
     *
     * @param tracer provides the tracer when tracing is enabled
     */
    public TraceIdResponseFilter(ObjectProvider<Tracer> tracer) {
        this.tracer = tracer;
    }

    /**
     * Sets the header before the request is processed, so it is present even on error responses.
     *
     * @param request     the current request
     * @param response    the current response
     * @param filterChain the remaining filters
     * @throws ServletException if a later filter fails
     * @throws IOException      if writing the response fails
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Tracer currentTracer = tracer.getIfAvailable();
        Span span = currentTracer != null ? currentTracer.currentSpan() : null;
        if (span != null) {
            response.setHeader(HEADER, span.context().traceId());
        }
        filterChain.doFilter(request, response);
    }
}
