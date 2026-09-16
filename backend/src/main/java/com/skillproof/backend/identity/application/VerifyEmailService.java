package com.skillproof.backend.identity.application;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.identity.api.VerifyEmailRequest;
import com.skillproof.backend.identity.api.VerifyEmailResponse;
import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.domain.EmailVerificationToken;
import com.skillproof.backend.identity.domain.UserAccount;
import com.skillproof.backend.identity.infrastructure.EmailVerificationTokenRepository;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class VerifyEmailService {

    private final EmailVerificationTokenRepository
            tokenRepository;

    private final UserAccountRepository
            userAccountRepository;

    private final EmailVerificationTokenCodec
            tokenCodec;

    public VerifyEmailService(
            EmailVerificationTokenRepository tokenRepository,
            UserAccountRepository userAccountRepository,
            EmailVerificationTokenCodec tokenCodec
    ) {
        this.tokenRepository =
                tokenRepository;

        this.userAccountRepository =
                userAccountRepository;

        this.tokenCodec =
                tokenCodec;
    }

    @Transactional
    public VerifyEmailResponse verify(
            VerifyEmailRequest request
    ) {

        Instant now = Instant.now();

        String tokenHash =
                tokenCodec.hash(
                        request.token()
                );

        EmailVerificationToken token =
                tokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(
                                this::invalidToken
                        );

        UserAccount account =
                userAccountRepository
                        .findByIdForUpdate(
                                token.getUserAccountId()
                        )
                        .orElseThrow(
                                this::invalidToken
                        );

        if (account.getStatus()
                == AccountStatus.ACTIVE) {

            throw new ConflictException(
                    "IDENTITY_ALREADY_VERIFIED",
                    "This email address is already verified."
            );
        }

        if (account.getStatus()
                == AccountStatus.DISABLED) {

            throw new ConflictException(
                    "IDENTITY_VERIFICATION_INVALID_STATE",
                    "This account cannot be verified in its current state."
            );
        }

        if (token.isInvalidated()
                || token.isConsumed()) {

            throw invalidToken();
        }

        if (token.isExpired(now)) {

            throw new BadRequestException(
                    "IDENTITY_VERIFICATION_EXPIRED",
                    "The email verification link has expired."
            );
        }

        int consumed =
                tokenRepository.consumeIfUsable(
                        token.getId(),
                        now
                );

        if (consumed != 1) {
            throw invalidToken();
        }

        account.verifyEmail(now);

        userAccountRepository.saveAndFlush(
                account
        );

        return new VerifyEmailResponse(
                account.getId(),
                account.getEmail(),
                account.getDisplayName(),
                account.getStatus(),
                account.getEmailVerifiedAt()
        );
    }

    private BadRequestException invalidToken() {

        return new BadRequestException(
                "IDENTITY_VERIFICATION_INVALID",
                "The email verification link is invalid."
        );
    }
}