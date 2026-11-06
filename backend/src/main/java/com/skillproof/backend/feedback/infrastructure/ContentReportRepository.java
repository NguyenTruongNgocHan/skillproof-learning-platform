package com.skillproof.backend.feedback.infrastructure;

import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface ContentReportRepository extends JpaRepository<ContentReport, UUID> {
  boolean existsByActorIdAndReviewId(UUID actor, UUID review);

  Page<ContentReport> findByStatusOrderByCreatedAtAsc(String status, Pageable page);

  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select e from ContentReport e where e.id=:id")
  Optional<ContentReport> lock(UUID id);
}
