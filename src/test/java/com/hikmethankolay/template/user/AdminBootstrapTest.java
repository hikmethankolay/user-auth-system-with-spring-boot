package com.hikmethankolay.template.user;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import com.hikmethankolay.template.TestProperties;
import com.hikmethankolay.template.common.ApiException;
import com.hikmethankolay.template.common.ProblemType;
import com.hikmethankolay.template.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {

    @Mock
    private UserService users;

    private AdminBootstrap bootstrap() {
        AppProperties p = TestProperties.create();
        return new AdminBootstrap(
                users,
                new AppProperties(
                        p.jwt(),
                        p.cookie(),
                        p.cors(),
                        p.login(),
                        p.rateLimit(),
                        p.account(),
                        p.mail(),
                        new AppProperties.Admin("owner", "owner@example.com", "Owner#12345")));
    }

    @Test
    void anotherInstanceCreatingTheOwnerFirstIsNotAnError() {
        when(users.ownerExists()).thenReturn(false, true);
        when(users.register(any(), any(), any(), any(), anyBoolean()))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatCode(() -> bootstrap().run(null)).doesNotThrowAnyException();
    }

    @Test
    void aConflictWithAnotherAccountStillStopsStartup() {
        when(users.ownerExists()).thenReturn(false);
        when(users.register(any(), any(), any(), any(), anyBoolean()))
                .thenThrow(ApiException.conflict(ProblemType.USERNAME_TAKEN, "Username is already taken."));

        assertThatThrownBy(() -> bootstrap().run(null)).isInstanceOf(ApiException.class);
    }
}
