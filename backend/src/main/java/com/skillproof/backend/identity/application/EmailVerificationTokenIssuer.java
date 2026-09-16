package com.skillproof.backend.identity.application;

import com.skillproof.backend.identity.domain.EmailVerificationToken;
import com.skillproof.backend.identity.infrastructure.EmailVerificationTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class EmailVerificationTokenIssuer {

    private final EmailVerificationTokenRepository
            tokenRepository;

    private final EmailVerificationTokenCodec tokenCodec;

    private final EmailVerificationProperties properties;

    public EmailVerificationTokenIssuer(
            EmailVerificationTokenRepository tokenRepository,
            EmailVerificationTokenCodec tokenCodec,
            EmailVerificationProperties properties
    ) {
        this.tokenRepository = tokenRepository;
        this.tokenCodec = tokenCodec;
        this.properties = properties;
    }

    public IssuedEmailVerification issue(
            UUID userAccountId,
            Instant now
    ) {

        tokenRepository.invalidateOutstandingTokens(
                userAccountId,
                now
        );

        String rawToken =
                tokenCodec.generateRawToken();

        String tokenHash =
                tokenCodec.hash(rawToken);

        Instant expiresAt =
                now.plus(properties.getTtl());

        EmailVerificationToken token =
                EmailVerificationToken.issue(
                        userAccountId,
                        tokenHash,
                        now,
                        expiresAt
                );

        tokenRepository.saveAndFlush(token);

        return new IssuedEmailVerification(
                rawToken,
                expiresAt
        );
    }

    public boolean canResend(
            UUID userAccountId,
            Instant now
    ) {

        return tokenRepository
                .findTopByUserAccountIdOrderByCreatedAtDesc(
                        userAccountId
                )
                .map(token ->
                        !now.isBefore(
                                token.getCreatedAt()
                                        .plus(
                                                properties
                                                        .getResendCooldown()
                                        )
                        )
                )
                .orElse(true);
    }
}