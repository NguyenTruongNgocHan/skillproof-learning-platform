package com.skillproof.backend.identity.application;

import com.skillproof.backend.identity.api.ResendVerificationRequest;
import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.domain.UserAccount;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class ResendEmailVerificationService {

    private final UserAccountRepository
            userAccountRepository;

    private final EmailVerificationTokenIssuer
            verificationTokenIssuer;

    private final ApplicationEventPublisher
            eventPublisher;

    public ResendEmailVerificationService(
            UserAccountRepository userAccountRepository,
            EmailVerificationTokenIssuer verificationTokenIssuer,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userAccountRepository =
                userAccountRepository;

        this.verificationTokenIssuer =
                verificationTokenIssuer;

        this.eventPublisher =
                eventPublisher;
    }

    @Transactional
    public void resend(
            ResendVerificationRequest request
    ) {

        String normalizedEmail =
                request.email()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        Optional<UserAccount> accountOptional =
                userAccountRepository
                        .findByEmailForUpdate(
                                normalizedEmail
                        );

        /*
         * Deliberately return the same external outcome
         * whether the account exists or not.
         *
         * This reduces account enumeration.
         */
        if (accountOptional.isEmpty()) {
            return;
        }

        UserAccount account =
                accountOptional.get();

        if (account.getStatus()
                != AccountStatus.PENDING_VERIFICATION) {
            return;
        }

        Instant now = Instant.now();

        if (!verificationTokenIssuer.canResend(
                account.getId(),
                now
        )) {
            return;
        }

        IssuedEmailVerification verification =
                verificationTokenIssuer.issue(
                        account.getId(),
                        now
                );

        eventPublisher.publishEvent(
                new VerificationEmailRequestedEvent(
                        account.getEmail(),
                        account.getDisplayName(),
                        verification.rawToken(),
                        verification.expiresAt()
                )
        );
    }
}