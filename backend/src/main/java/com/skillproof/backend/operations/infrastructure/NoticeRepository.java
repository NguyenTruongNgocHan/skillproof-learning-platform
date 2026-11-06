package com.skillproof.backend.operations.infrastructure;

import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface NoticeRepository extends JpaRepository<WorkspaceNotice, UUID> {
  long countByRecipientIdAndReadAtIsNull(UUID actor);

  Page<WorkspaceNotice> findByRecipientIdOrderByCreatedAtDesc(UUID actor, Pageable page);
}
