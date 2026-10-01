package com.skillproof.backend.identity.contract;

import java.util.Optional;
import java.util.UUID;

public interface IdentityAccessQuery {

    record Account(UUID id, String email, String role, String status) {

        public boolean active() {
            return "ACTIVE".equals(status);
        }
    }

    Optional<Account> find(UUID userId);

    Optional<Account> findByEmail(String email);

    default boolean isActiveLearner(UUID userId) {
        return find(userId).filter(Account::active).map(a -> "LEARNER".equals(a.role())).orElse(false);
    }

    default boolean isActiveOrganizer(UUID userId) {
        return find(userId).filter(Account::active).map(a -> "ORGANIZER".equals(a.role())).orElse(false);
    }

    default boolean isActiveAdmin(UUID userId) {
        return find(userId).filter(Account::active).map(a -> "ADMIN".equals(a.role())).orElse(false);
    }
}
