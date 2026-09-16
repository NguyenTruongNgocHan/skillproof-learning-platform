package com.skillproof.backend.identity.application;

import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.identity.api.RegisterRequest;
import com.skillproof.backend.identity.api.RegisterResponse;
import com.skillproof.backend.identity.domain.UserAccount;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class RegisterUserService {

    private static final String ALREADY_EXISTS_CODE =
            "IDENTITY_ALREADY_EXISTS";

    private static final String ALREADY_EXISTS_MESSAGE =
            "An account with this email already exists.";

    private final UserAccountRepository
            userAccountRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailVerificationTokenIssuer
            verificationTokenIssuer;

    private final ApplicationEventPublisher
            eventPublisher;

    public RegisterUserService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            EmailVerificationTokenIssuer verificationTokenIssuer,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userAccountRepository =
                userAccountRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.verificationTokenIssuer =
                verificationTokenIssuer;

        this.eventPublisher =
                eventPublisher;
    }

    @Transactional
    public RegisterResponse register(
            RegisterRequest request
    ) {

        String normalizedEmail =
                normalizeEmail(request.email());

        String displayName =
                normalizeDisplayName(
                        request.displayName()
                );

        ensureEmailAvailable(normalizedEmail);

        String passwordHash =
                passwordEncoder.encode(
                        request.password()
                );

        UserAccount account =
                UserAccount.newLearner(
                        normalizedEmail,
                        passwordHash,
                        displayName
                );

        UserAccount savedAccount;

        try {

            savedAccount =
                    userAccountRepository
                            .saveAndFlush(account);

        } catch (DataIntegrityViolationException exception) {

            throw emailAlreadyExists();
        }

        Instant now = Instant.now();

        IssuedEmailVerification verification =
                verificationTokenIssuer.issue(
                        savedAccount.getId(),
                        now
                );

        eventPublisher.publishEvent(
                new VerificationEmailRequestedEvent(
                        savedAccount.getEmail(),
                        savedAccount.getDisplayName(),
                        verification.rawToken(),
                        verification.expiresAt()
                )
        );

        return toResponse(savedAccount);
    }

    private void ensureEmailAvailable(
            String normalizedEmail
    ) {

        if (userAccountRepository
                .existsByEmail(normalizedEmail)) {

            throw emailAlreadyExists();
        }
    }

    private ConflictException emailAlreadyExists() {

        return new ConflictException(
                ALREADY_EXISTS_CODE,
                ALREADY_EXISTS_MESSAGE
        );
    }

    private RegisterResponse toResponse(
            UserAccount account
    ) {

        return new RegisterResponse(
                account.getId(),
                account.getEmail(),
                account.getDisplayName(),
                account.getRole(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }

    private String normalizeEmail(
            String email
    ) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeDisplayName(
            String displayName
    ) {

        return displayName.trim();
    }
}