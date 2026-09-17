package com.skillproof.backend.identity.application;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.identity.api.ResetPasswordRequest;
import com.skillproof.backend.identity.infrastructure.AuthSessionRepository;
import com.skillproof.backend.identity.infrastructure.PasswordResetTokenRepository;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ResetPasswordService {

    private final PasswordResetTokenRepository tokens;
    private final PasswordResetTokenCodec codec;
    private final UserAccountRepository users;
    private final AuthSessionRepository sessions;
    private final PasswordEncoder passwordEncoder;
    private final AuditService audit;

    public ResetPasswordService(
            PasswordResetTokenRepository tokens,
            PasswordResetTokenCodec codec,
            UserAccountRepository users,
            AuthSessionRepository sessions,
            PasswordEncoder passwordEncoder,
            AuditService audit
    ) {
        this.tokens = tokens;
        this.codec = codec;
        this.users = users;
        this.sessions = sessions;
        this.passwordEncoder = passwordEncoder;
        this.audit = audit;
    }

    @Transactional
    public void reset(ResetPasswordRequest request, RequestMetadata metadata) {
        Instant now = Instant.now();
        var token = tokens.findByTokenHash(codec.hash(request.token()))
                .orElseThrow(this::invalidToken);

        if (!token.isUsable(now)) {
            throw invalidToken();
        }

        if (tokens.consumeIfUsable(token.getId(), now) != 1) {
            throw invalidToken();
        }

        var user = users.findByIdForUpdate(token.getUserAccountId())
                .orElseThrow(this::invalidToken);

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        sessions.findAllByUserAccountId(user.getId())
                .forEach(session -> session.revoke(now));
        tokens.invalidateOutstandingTokens(user.getId(), now);
        audit.record(
                user.getId(),
                "PASSWORD_RESET_SUCCEEDED",
                "SUCCESS",
                user.getEmail(),
                metadata,
                null
        );
    }

    private BadRequestException invalidToken() {
        return new BadRequestException(
                "IDENTITY_PASSWORD_RESET_INVALID",
                "The password reset link is invalid or has expired."
        );
    }
}
