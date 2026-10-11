package com.skillproof.backend.feedback.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface ReviewModerationRepository extends JpaRepository<ReviewModerationEntry, UUID> {
    List<ReviewModerationEntry> findByReviewIdOrderByCreatedAtDesc(UUID reviewId);
}
