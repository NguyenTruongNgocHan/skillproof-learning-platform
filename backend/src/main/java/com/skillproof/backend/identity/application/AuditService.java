package com.skillproof.backend.identity.application;

import com.skillproof.backend.identity.domain.SecurityAuditEvent;
import com.skillproof.backend.identity.infrastructure.SecurityAuditEventRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

  private final SecurityAuditEventRepository repository;

  public AuditService(SecurityAuditEventRepository repository) {
    this.repository = repository;
  }

  public void record(
      UUID userId,
      String type,
      String outcome,
      String subject,
      RequestMetadata metadata,
      String details) {
    repository.save(
        SecurityAuditEvent.record(
            userId,
            type,
            outcome,
            subject,
            metadata.ipAddress(),
            metadata.userAgent(),
            details,
            Instant.now()));
  }
}
