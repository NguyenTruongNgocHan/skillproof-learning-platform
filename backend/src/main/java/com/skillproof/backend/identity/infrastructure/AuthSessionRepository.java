package com.skillproof.backend.identity.infrastructure;

import com.skillproof.backend.identity.domain.AuthSession;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

  List<AuthSession> findAllByUserAccountId(UUID userAccountId);
}
