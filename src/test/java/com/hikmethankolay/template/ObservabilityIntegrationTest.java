package com.hikmethankolay.template;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hikmethankolay.template.common.TraceIdResponseFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;

/** Tracing and metrics are disabled in tests by default; this test turns them on and checks both. */
@AutoConfigureTracing
@AutoConfigureMetrics
class ObservabilityIntegrationTest extends IntegrationTest {

    @Test
    void everyResponseCarriesTraceId() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(TraceIdResponseFilter.HEADER, matchesPattern("[0-9a-f]{32}")));
    }

    @Test
    void prometheusEndpointExposesHttpMetrics() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());

        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("http_server_requests_seconds_count")))
                .andExpect(content().string(containsString("application=\"spring-boot-template\"")));
    }
}
