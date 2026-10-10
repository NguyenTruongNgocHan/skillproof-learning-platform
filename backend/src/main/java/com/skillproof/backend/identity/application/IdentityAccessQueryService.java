package com.skillproof.backend.identity.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;

@Service
public class IdentityAccessQueryService implements IdentityAccessQuery {

    private final UserAccountRepository accounts;

    public IdentityAccessQueryService(UserAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void lockActiveAccount(UUID userId) {
        var user = accounts.findByIdForUpdate(userId).orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Active account required"));
        if (!user.isActive()) {
            throw new org.springframework.security.access.AccessDeniedException("Active account required");
        }
    }

    private Account view(com.skillproof.backend.identity.domain.UserAccount a) {
        return new Account(a.getId(), a.getEmail(), a.getRole().name(), a.getStatus().name());
    }

    public Optional<Account> find(UUID id) {
        return accounts.findById(id).map(this::view);
    }

    public java.util.List<UUID> findAccountIdsByEmailFragment(String query) {
        return accounts.findAccountIdsByEmailFragment(query);
    }

    public Optional<Account> findByEmail(String email) {
        return accounts.findByEmail(email).map(this::view);
    }
}
