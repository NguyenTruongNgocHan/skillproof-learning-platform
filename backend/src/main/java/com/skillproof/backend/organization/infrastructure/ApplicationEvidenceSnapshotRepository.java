package com.skillproof.backend.organization.infrastructure;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationEvidenceSnapshotRepository
    extends JpaRepository<ApplicationEvidenceSnapshot, UUID> {
  List<ApplicationEvidenceSnapshot> findByRevisionId(UUID revisionId);
}
