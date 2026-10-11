package com.skillproof.backend.organization.infrastructure;

import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface EvidenceRepository extends JpaRepository<EvidenceDocument, UUID> {
  boolean existsByOrganizationIdAndMediaId(UUID organizationId, UUID mediaId);

  List<EvidenceDocument> findByOrganizationIdOrderByCreatedAtDesc(UUID org);
}
