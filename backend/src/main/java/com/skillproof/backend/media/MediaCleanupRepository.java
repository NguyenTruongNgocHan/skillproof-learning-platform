package com.skillproof.backend.media;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface MediaCleanupRepository extends JpaRepository<MediaCleanupTask, UUID> {
    java.util.List<MediaCleanupTask> findByNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
        java.time.Instant now,
        org.springframework.data.domain.Pageable page
    );
}
