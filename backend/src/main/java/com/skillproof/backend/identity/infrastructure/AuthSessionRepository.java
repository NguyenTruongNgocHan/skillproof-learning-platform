package com.skillproof.backend.identity.infrastructure;

import com.skillproof.backend.identity.domain.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    List<AuthSession> findAllByUserAccountId(UUID userAccountId);
}
