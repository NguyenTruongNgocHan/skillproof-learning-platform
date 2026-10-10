package com.skillproof.backend.identity.application;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.UnauthorizedException;
import com.skillproof.backend.identity.application.model.AuthResponse;
import com.skillproof.backend.identity.application.model.LoginCommand;
import com.skillproof.backend.identity.domain.AuthSession;
import com.skillproof.backend.identity.domain.RefreshToken;
import com.skillproof.backend.identity.domain.UserAccount;
import com.skillproof.backend.identity.infrastructure.AuthSessionRepository;
import com.skillproof.backend.identity.infrastructure.RefreshTokenRepository;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;

@Service
public class AuthenticationService {

    private final UserAccountRepository users;
    private final AuthSessionRepository sessions;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final TokenCodec tokens;
    private final TokenProperties properties;
    private final AuditService audit;

    public AuthenticationService(UserAccountRepository users, AuthSessionRepository sessions,
            RefreshTokenRepository refreshTokens, PasswordEncoder passwordEncoder,
            TokenCodec tokens, TokenProperties properties, AuditService audit) {
        this.users = users;
        this.sessions = sessions;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.properties = properties;
        this.audit = audit;
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public IssuedSession login(LoginCommand request, RequestMetadata metadata) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        UserAccount user = users.findByEmailForUpdate(email).orElseThrow(this::invalidCredentials);
        Instant now = Instant.now();

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            user.recordFailedLogin(now);
            audit.record(user.getId(), "LOGIN", "FAILURE", email, metadata, "INVALID_CREDENTIALS");
            throw invalidCredentials();
        }
        if (user.isPendingVerification()) {
            throw new UnauthorizedException("IDENTITY_EMAIL_NOT_VERIFIED", "Verify your email before signing in.");
        }
        if (!user.isActive() || user.isLocked(now)) {
            throw new UnauthorizedException("IDENTITY_ACCOUNT_UNAVAILABLE", "This account cannot sign in.");
        }

        user.recordSuccessfulLogin(now);
        var issued = createSession(user, metadata, now);
        audit.record(user.getId(), "LOGIN", "SUCCESS", email, metadata, null);
        return issued;
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public IssuedSession refresh(String rawToken, RequestMetadata metadata) {
        if (rawToken == null || rawToken.isBlank()) {
            throw invalidRefresh();
        }
        Instant now = Instant.now();
        RefreshToken current = refreshTokens.findByTokenHashForUpdate(tokens.hash(rawToken))
                .orElseThrow(this::invalidRefresh);

        if (current.wasConsumed()) {
            refreshTokens.findAllByFamilyId(current.getFamilyId()).forEach(token -> token.revoke(now));
            sessions.findById(current.getSessionId()).ifPresent(session -> session.revoke(now));
            throw new UnauthorizedException("IDENTITY_REFRESH_REUSE", "Refresh token reuse was detected.");
        }
        if (!current.isUsable(now)) {
            throw invalidRefresh();
        }

        AuthSession session = sessions.findById(current.getSessionId()).orElseThrow(this::invalidRefresh);
        if (!session.isUsable(now)) {
            throw invalidRefresh();
        }
        UserAccount user = users.findById(session.getUserAccountId()).orElseThrow(this::invalidRefresh);
        if (!user.isActive()) {
            throw invalidRefresh();
        }

        String nextRaw = tokens.randomRefreshToken();
        RefreshToken next = RefreshToken.issue(session.getId(), current.getFamilyId(), tokens.hash(nextRaw),
                now, now.plus(properties.getRefreshTtl()));
        refreshTokens.save(next);
        current.consume(now, next.getId());
        session.touch(now);
        return new IssuedSession(response(user, session.getId(), now), nextRaw);
    }

    @Transactional
    public void logout(String rawRefreshToken, String rawAccessToken) {
        Instant now = Instant.now();
        UUID accessSessionId = accessSessionId(rawAccessToken);
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokens.findByTokenHash(tokens.hash(rawRefreshToken)).ifPresent(token -> {
                refreshTokens.findAllByFamilyId(token.getFamilyId())
                        .forEach(value -> value.revoke(now));
                sessions.findById(token.getSessionId()).ifPresent(session -> session.revoke(now));
            });
        }
        if (accessSessionId != null) {
            refreshTokens.findAllBySessionId(accessSessionId).forEach(token -> token.revoke(now));
            sessions.findById(accessSessionId).ifPresent(session -> session.revoke(now));
        }
        if (rawRefreshToken != null || accessSessionId != null) {
            audit.record(null, "LOGOUT", "SUCCESS", null,
                    new RequestMetadata(null, null), null);
        }
    }

    public IssuedSession createSession(UserAccount user, RequestMetadata metadata, Instant now) {
        AuthSession session = sessions.save(AuthSession.start(user.getId(), metadata.userAgent(),
                metadata.ipAddress(), now, now.plus(properties.getRefreshTtl())));
        String rawRefresh = tokens.randomRefreshToken();
        refreshTokens.save(RefreshToken.issue(session.getId(), UUID.randomUUID(), tokens.hash(rawRefresh),
                now, now.plus(properties.getRefreshTtl())));
        return new IssuedSession(response(user, session.getId(), now), rawRefresh);
    }

    private AuthResponse response(UserAccount user, UUID sessionId, Instant now) {
        return new AuthResponse(tokens.accessToken(user, sessionId, now), properties.getAccessTtl().toSeconds(),
                new AuthResponse.UserSummary(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole(), user.getStatus()));
    }

    private UUID accessSessionId(String rawAccessToken) {
        if (rawAccessToken == null || rawAccessToken.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(tokens.verifyAccessToken(rawAccessToken)
                    .getClaim("sessionId").asString());
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private UnauthorizedException invalidCredentials() {
        return new UnauthorizedException("IDENTITY_INVALID_CREDENTIALS", "Email or password is incorrect.");
    }

    private UnauthorizedException invalidRefresh() {
        return new UnauthorizedException("IDENTITY_INVALID_REFRESH_TOKEN", "The session is invalid or expired.");
    }
}
