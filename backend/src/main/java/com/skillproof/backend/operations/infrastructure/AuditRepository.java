package com.skillproof.backend.operations.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.*;
import java.util.*;

public interface AuditRepository extends JpaRepository<AuditEntry, UUID> {
    Page<AuditEntry> findByOrganizationIdOrderByCreatedAtDesc(UUID organization, Pageable page);
}
