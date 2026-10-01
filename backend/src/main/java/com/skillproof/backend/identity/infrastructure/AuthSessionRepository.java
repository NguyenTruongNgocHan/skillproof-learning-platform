package com.skillproof.backend.identity.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillproof.backend.identity.domain.AuthSession;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    List<AuthSession> findAllByUserAccountId(UUID userAccountId);
}
