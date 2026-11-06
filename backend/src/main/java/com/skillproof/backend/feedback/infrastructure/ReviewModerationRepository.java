package com.skillproof.backend.feedback.infrastructure;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewModerationRepository extends JpaRepository<ReviewModerationEntry, UUID> {
  List<ReviewModerationEntry> findByReviewIdOrderByCreatedAtDesc(UUID reviewId);
}
