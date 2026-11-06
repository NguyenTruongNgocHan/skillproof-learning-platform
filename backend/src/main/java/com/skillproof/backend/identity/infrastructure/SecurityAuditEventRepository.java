package com.skillproof.backend.identity.infrastructure;

import com.skillproof.backend.identity.domain.SecurityAuditEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityAuditEventRepository extends JpaRepository<SecurityAuditEvent, UUID> {}
