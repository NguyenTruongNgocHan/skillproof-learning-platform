package com.skillproof.backend.organization.infrastructure;

import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface IssuerApplicationRepository extends JpaRepository<IssuerApplication, UUID> {
  List<IssuerApplication> findByOrganizationIdOrderByCreatedAtDesc(UUID org);

  List<IssuerApplication> findByStatusOrderByCreatedAtAsc(String status);
}
