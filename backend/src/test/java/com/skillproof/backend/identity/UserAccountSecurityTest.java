package com.skillproof.backend.identity;

import com.skillproof.backend.identity.domain.UserAccount;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserAccountSecurityTest {
    @Test
    void locksAfterFiveFailedLoginsAndResetsAfterSuccess() {
        Instant now = Instant.parse("2026-09-16T10:00:00Z");
        UserAccount account = UserAccount.newOAuthLearner("learner@example.com", "hash", "Learner", now);

        for (int attempt = 0; attempt < 5; attempt++) account.recordFailedLogin(now);
        assertThat(account.isLocked(now.plusSeconds(1))).isTrue();

        account.recordSuccessfulLogin(now.plusSeconds(2));
        assertThat(account.isLocked(now.plusSeconds(3))).isFalse();
        assertThat(account.getLastLoginAt()).isEqualTo(now.plusSeconds(2));
    }
}
