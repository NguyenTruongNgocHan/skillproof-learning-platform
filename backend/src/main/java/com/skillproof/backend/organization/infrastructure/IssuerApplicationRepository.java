package com.skillproof.backend.organization.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.*;
import java.util.*;

public interface IssuerApplicationRepository extends JpaRepository<IssuerApplication, UUID> {
    List<IssuerApplication> findByOrganizationIdOrderByCreatedAtDesc(UUID org);

    List<IssuerApplication> findByStatusOrderByCreatedAtAsc(String status);
}
