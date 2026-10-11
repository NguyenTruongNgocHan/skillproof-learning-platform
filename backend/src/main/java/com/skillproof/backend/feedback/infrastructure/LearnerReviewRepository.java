package com.skillproof.backend.feedback.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.*;
import java.util.*;

public interface LearnerReviewRepository extends JpaRepository<LearnerReview, UUID> {
    Optional<LearnerReview> findByActorIdAndTypeAndTargetId(UUID actor, String type, UUID target);

    Page<LearnerReview> findByTypeAndTargetIdAndStatusOrderByCreatedAtDesc(
            String type, UUID target, String status, Pageable page);

    Page<LearnerReview> findByOrganizationIdOrderByCreatedAtDesc(UUID org, Pageable page);

    @Query(
            "select avg(r.rating), count(r) from LearnerReview r where r.type=:type and"
                    + " r.targetId=:target and r.status='VISIBLE'")
    List<Object[]> summary(String type, UUID target);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from LearnerReview e where e.id=:id")
    Optional<LearnerReview> lock(UUID id);
}
