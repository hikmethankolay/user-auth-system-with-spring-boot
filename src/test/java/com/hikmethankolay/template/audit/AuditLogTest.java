package com.hikmethankolay.template.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(OutputCaptureExtension.class)
class AuditLogTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final AuditLog audit = new AuditLog(registry);

    @AfterEach
    void cleanUp() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private double count(AuditEvent event) {
        var counter = registry.find(AuditLog.METRIC_NAME).tag("event", event.action()).counter();
        return counter == null ? 0 : counter.count();
    }

    @Test
    void writesEventWithActorIpAndDetails(CapturedOutput output) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.7");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("1", null, "ROLE_ADMIN"));

        audit.record(AuditEvent.ROLE_CHANGED, 42L, "from", "USER", "to", "ADMIN");

        assertThat(output).contains(
                "event=admin.role_changed userId=42 actorId=1 clientIp=203.0.113.7 from=USER to=ADMIN");
        assertThat(count(AuditEvent.ROLE_CHANGED)).isEqualTo(1);
    }

    @Test
    void waitsForTheTransactionToCommit(CapturedOutput output) {
        TransactionSynchronizationManager.initSynchronization();

        audit.record(AuditEvent.PASSWORD_CHANGED, 7L);
        assertThat(output).doesNotContain("user.password_changed");
        assertThat(count(AuditEvent.PASSWORD_CHANGED)).isZero();

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        assertThat(output).contains("event=user.password_changed userId=7");
        assertThat(count(AuditEvent.PASSWORD_CHANGED)).isEqualTo(1);
    }

    @Test
    void rejectsUnpairedDetails() {
        assertThatThrownBy(() -> audit.record(AuditEvent.LOGOUT, 1L, "orphan"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
