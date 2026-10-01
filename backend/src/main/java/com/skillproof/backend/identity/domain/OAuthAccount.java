package com.skillproof.backend.identity.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "oauth_account")
public class OAuthAccount {

    @Id
    private UUID id;
    private UUID userAccountId;
    private String provider;
    private String providerSubject;
    private String providerEmail;
    private Instant createdAt;

    protected OAuthAccount() {
    }

    public static OAuthAccount link(UUID userId, String provider, String subject, String email, Instant now) {
        var account = new OAuthAccount();
        account.id = UUID.randomUUID();
        account.userAccountId = userId;
        account.provider = provider;
        account.providerSubject = subject;
        account.providerEmail = email;
        account.createdAt = now;
        return account;
    }

    public UUID getUserAccountId() {
        return userAccountId;
    }
}
