package com.skillproof.backend.identity.infrastructure;

import com.skillproof.backend.identity.domain.SecurityAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SecurityAuditEventRepository extends JpaRepository<SecurityAuditEvent, UUID> {
}
