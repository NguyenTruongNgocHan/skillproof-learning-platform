package com.skillproof.backend.media.infrastructure;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.*;
import java.util.*;

public interface DirectUploadRepository extends JpaRepository<DirectUploadTicket, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from DirectUploadTicket t where t.id=:id")
    Optional<DirectUploadTicket> lock(UUID id);

    java.util.List<DirectUploadTicket>
            findTop100ByExpiresAtBeforeAndMultipartIdIsNotNullAndAssembledFalseOrderByExpiresAt(
                    java.time.Instant now);
}
