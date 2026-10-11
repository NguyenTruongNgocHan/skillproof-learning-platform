package com.skillproof.backend.operations.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.*;
import java.util.*;

public interface NoticeRepository extends JpaRepository<WorkspaceNotice, UUID> {
    long countByRecipientIdAndReadAtIsNull(UUID actor);

    Page<WorkspaceNotice> findByRecipientIdOrderByCreatedAtDesc(UUID actor, Pageable page);
}
