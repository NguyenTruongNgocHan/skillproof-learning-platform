package com.skillproof.backend.identity.application;

import com.skillproof.backend.common.exception.UnauthorizedException;
import com.skillproof.backend.identity.domain.OAuthAccount;
import com.skillproof.backend.identity.domain.UserAccount;
import com.skillproof.backend.identity.infrastructure.OAuthAccountRepository;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class OAuthLoginService {

    private final OAuthAccountRepository oauthAccounts;
    private final UserAccountRepository users;
    private final PasswordEncoder passwords;
    private final AuthenticationService authentication;
    private final AuditService audit;

    public OAuthLoginService(OAuthAccountRepository oauthAccounts, UserAccountRepository users,
            PasswordEncoder passwords, AuthenticationService authentication,
            AuditService audit) {
        this.oauthAccounts = oauthAccounts;
        this.users = users;
        this.passwords = passwords;
        this.authentication = authentication;
        this.audit = audit;
    }

    @Transactional
    public IssuedSession login(String provider, String subject, String email, String displayName,
            boolean emailVerified, RequestMetadata metadata) {
        if (email == null || !emailVerified) {
            throw new UnauthorizedException("IDENTITY_OAUTH_EMAIL_UNVERIFIED",
                    "The OAuth provider must supply a verified email address.");
        }
        Instant now = Instant.now();
        var linked = oauthAccounts.findByProviderAndProviderSubject(provider, subject);
        UserAccount user;
        if (linked.isPresent()) {
            user = users.findById(linked.get().getUserAccountId()).orElseThrow();
        } else {
            String normalized = email.trim().toLowerCase(Locale.ROOT);
            user = users.findByEmail(normalized).orElseGet(() -> users.save(
                    UserAccount.newOAuthLearner(normalized, passwords.encode(UUID.randomUUID().toString()),
                            displayName == null || displayName.isBlank() ? normalized.split("@")[0] : displayName, now)
            ));
            oauthAccounts.save(OAuthAccount.link(user.getId(), provider, subject, normalized, now));
        }
        if (!user.isActive() || user.isLocked(now)) {
            throw new UnauthorizedException(
                    "IDENTITY_ACCOUNT_UNAVAILABLE",
                    "This account cannot sign in."
            );
        }
        var session = authentication.createSession(user, metadata, now);
        audit.record(user.getId(), "OAUTH_LOGIN", "SUCCESS", user.getEmail(),
                metadata, provider);
        return session;
    }
}
