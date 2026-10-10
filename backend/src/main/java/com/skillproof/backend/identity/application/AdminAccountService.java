package com.skillproof.backend.identity.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.identity.application.model.AdminAccountResponse;
import com.skillproof.backend.identity.contract.OrganizationRoleTransitionPolicy;
import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.domain.UserRole;
import com.skillproof.backend.identity.infrastructure.AuthSessionRepository;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;

@Service
public class AdminAccountService {

    private final UserAccountRepository users;
    private final AuthSessionRepository sessions;
    private final AuditService audit;
    private final OrganizationRoleTransitionPolicy organizationRolePolicy;

    public AdminAccountService(
            UserAccountRepository users,
            AuthSessionRepository sessions,
            AuditService audit,
            OrganizationRoleTransitionPolicy organizationRolePolicy
    ) {
        this.users = users;
        this.sessions = sessions;
        this.audit = audit;
        this.organizationRolePolicy = organizationRolePolicy;
    }

    @Transactional(readOnly = true)
    public Page<AdminAccountResponse> list(Pageable pageable) {
        return users.findAll(pageable).map(AdminAccountResponse::from);
    }

    @Transactional
    public AdminAccountResponse changeStatus(
            UUID actorId,
            UUID accountId,
            AccountStatus status,
            RequestMetadata metadata
    ) {
        if (actorId.equals(accountId) && status == AccountStatus.DISABLED) {
            throw new BadRequestException(
                    "IDENTITY_ADMIN_SELF_DISABLE",
                    "Administrators cannot disable their own account."
            );
        }

        var account = users.findByIdForUpdate(accountId)
                .orElseThrow(this::accountNotFound);

        if (status == AccountStatus.DISABLED) {
            account.disable();
            revokeSessions(accountId);
        } else if (status == AccountStatus.ACTIVE) {
            if (account.getEmailVerifiedAt() == null) {
                throw new com.skillproof.backend.common.exception.ConflictException(
                        "IDENTITY_EMAIL_NOT_VERIFIED", "Verify email before activating the account.");
            }
            account.activate();
        } else {
            throw new BadRequestException(
                    "IDENTITY_STATUS_TRANSITION_INVALID",
                    "Only ACTIVE and DISABLED are supported by this operation."
            );
        }

        audit.record(actorId, "ACCOUNT_STATUS_CHANGED", "SUCCESS",
                account.getEmail(), metadata, status.name());
        return AdminAccountResponse.from(account);
    }

    @Transactional
    public AdminAccountResponse changeRole(
            UUID actorId,
            UUID accountId,
            UserRole role,
            RequestMetadata metadata
    ) {
        if (actorId.equals(accountId)) {
            throw new BadRequestException(
                    "IDENTITY_ADMIN_SELF_ROLE_CHANGE",
                    "Administrators cannot change their own role."
            );
        }

        var account = users.findByIdForUpdate(accountId)
                .orElseThrow(this::accountNotFound);
        organizationRolePolicy.assertTransitionAllowed(accountId, role);
        account.changeRole(role);
        revokeSessions(accountId);
        audit.record(actorId, "ACCOUNT_ROLE_CHANGED", "SUCCESS",
                account.getEmail(), metadata, role.name());
        return AdminAccountResponse.from(account);
    }

    private void revokeSessions(UUID userId) {
        Instant now = Instant.now();
        sessions.findAllByUserAccountId(userId)
                .forEach(session -> session.revoke(now));
    }

    private NotFoundException accountNotFound() {
        return new NotFoundException(
                "IDENTITY_ACCOUNT_NOT_FOUND",
                "The requested account does not exist."
        );
    }
}
