package com.skillproof.backend.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_account")
public class UserAccount {

    @Id
    private UUID id;

    @Column(
            name = "email",
            nullable = false,
            unique = true,
            length = 320
    )
    private String email;

    @Column(
            name = "password_hash",
            nullable = false,
            length = 100
    )
    private String passwordHash;

    @Column(
            name = "display_name",
            nullable = false,
            length = 100
    )
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 32
    )
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 32
    )
    private AccountStatus status;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    protected UserAccount() {
    }

    private UserAccount(
            UUID id,
            String email,
            String passwordHash,
            String displayName,
            UserRole role,
            AccountStatus status
    ) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.displayName = Objects.requireNonNull(displayName);
        this.role = Objects.requireNonNull(role);
        this.status = Objects.requireNonNull(status);
    }

    public static UserAccount newLearner(
            String email,
            String passwordHash,
            String displayName
    ) {
        return new UserAccount(
                UUID.randomUUID(),
                email,
                passwordHash,
                displayName,
                UserRole.LEARNER,
                AccountStatus.PENDING_VERIFICATION
        );
    }

    public static UserAccount newAccount(
            String email,
            String passwordHash,
            String displayName,
            UserRole role
    ) {
        if (role == UserRole.ADMIN) {
            throw new IllegalArgumentException("Administrator accounts cannot be self-registered.");
        }
        return new UserAccount(
                UUID.randomUUID(),
                email,
                passwordHash,
                displayName,
                role,
                AccountStatus.PENDING_VERIFICATION
        );
    }

    public void verifyEmail(Instant verifiedAt) {

        Objects.requireNonNull(verifiedAt);

        if (status != AccountStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException(
                    "Only a pending account can be email verified."
            );
        }

        this.status = AccountStatus.ACTIVE;
        this.emailVerifiedAt = verifiedAt;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isPendingVerification() {
        return status == AccountStatus.PENDING_VERIFICATION;
    }

    public static UserAccount newOAuthLearner(String email, String passwordHash, String displayName, Instant now) {
        UserAccount account = new UserAccount(UUID.randomUUID(), email, passwordHash, displayName,
                UserRole.LEARNER, AccountStatus.ACTIVE);
        account.emailVerifiedAt = now;
        return account;
    }

    public static UserAccount newAdmin(String email, String passwordHash, String displayName, Instant now) {
        UserAccount account = new UserAccount(UUID.randomUUID(), email, passwordHash, displayName,
                UserRole.ADMIN, AccountStatus.ACTIVE);
        account.emailVerifiedAt = now;
        return account;
    }

    public boolean isLocked(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public void recordFailedLogin(Instant now) {
        failedLoginCount++;
        if (failedLoginCount >= 5) {
            lockedUntil = now.plusSeconds(900);
        }
    }

    public void recordSuccessfulLogin(Instant now) {
        failedLoginCount = 0;
        lockedUntil = null;
        lastLoginAt = now;
    }

    public void updateDisplayName(String value) {
        displayName = Objects.requireNonNull(value).trim();
    }

    public void disable() {
        status = AccountStatus.DISABLED;
        lockedUntil = null;
    }

    public void activate() {
        if (emailVerifiedAt == null) {
            throw new IllegalStateException(
                    "An unverified account cannot be activated."
            );
        }
        status = AccountStatus.ACTIVE;
        failedLoginCount = 0;
        lockedUntil = null;
    }

    public void changeRole(UserRole nextRole) {
        role = Objects.requireNonNull(nextRole);
    }

    public void changePassword(String nextPasswordHash) {
        passwordHash = Objects.requireNonNull(nextPasswordHash);
        failedLoginCount = 0;
        lockedUntil = null;
    }

    @PrePersist
    void prePersist() {

        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserRole getRole() {
        return role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
}
