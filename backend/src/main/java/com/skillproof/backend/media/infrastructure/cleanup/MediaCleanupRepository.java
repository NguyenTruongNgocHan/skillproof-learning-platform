package com.skillproof.backend.media.infrastructure.cleanup;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaCleanupRepository extends JpaRepository<MediaCleanupTask, UUID> {

    java.util.List<MediaCleanupTask> findByNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            java.time.Instant now,
            org.springframework.data.domain.Pageable page
    );

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select t from MediaCleanupTask t where t.id = :id")
    java.util.Optional<MediaCleanupTask> lock(java.util.UUID id);
}
