package com.skillproof.backend.operations.infrastructure;

import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface AuditRepository extends JpaRepository<AuditEntry, UUID> {
  Page<AuditEntry> findByOrganizationIdOrderByCreatedAtDesc(UUID organization, Pageable page);
}
