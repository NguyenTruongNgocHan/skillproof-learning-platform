package com.skillproof.backend.identity.application;

import com.skillproof.backend.identity.api.ForgotPasswordRequest;
import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class ForgotPasswordService {

    private final UserAccountRepository users;
    private final PasswordResetTokenIssuer tokenIssuer;
    private final ApplicationEventPublisher events;
    private final AuditService audit;

    public ForgotPasswordService(
            UserAccountRepository users,
            PasswordResetTokenIssuer tokenIssuer,
            ApplicationEventPublisher events,
            AuditService audit
    ) {
        this.users = users;
        this.tokenIssuer = tokenIssuer;
        this.events = events;
        this.audit = audit;
    }

    @Transactional
    public void request(ForgotPasswordRequest request, RequestMetadata metadata) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        users.findByEmailForUpdate(email)
                .filter(user -> user.getStatus() == AccountStatus.ACTIVE)
                .ifPresent(user -> {
                    Instant now = Instant.now();
                    if (!tokenIssuer.canIssue(user.getId(), now)) {
                        audit.record(
                                user.getId(),
                                "PASSWORD_RESET_REQUESTED",
                                "THROTTLED",
                                user.getEmail(),
                                metadata,
                                null
                        );
                        return;
                    }
                    IssuedPasswordReset issued = tokenIssuer.issue(user.getId(), now);
                    events.publishEvent(new PasswordResetEmailRequestedEvent(
                            user.getEmail(),
                            user.getDisplayName(),
                            issued.rawToken(),
                            issued.expiresAt()
                    ));
                    audit.record(
                            user.getId(),
                            "PASSWORD_RESET_REQUESTED",
                            "SUCCESS",
                            user.getEmail(),
                            metadata,
                            null
                    );
                });
    }
}
