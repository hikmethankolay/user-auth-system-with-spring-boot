package com.hikmethankolay.template;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** The connection pool must apply the server-side timeouts from application.yml to every connection. */
class DatabaseSettingsIntegrationTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void connectionsHaveStatementAndIdleTransactionTimeouts() {
        assertThat(jdbc.queryForObject("show statement_timeout", String.class)).isEqualTo("30s");
        assertThat(jdbc.queryForObject("show idle_in_transaction_session_timeout", String.class)).isEqualTo("1min");
    }
}
