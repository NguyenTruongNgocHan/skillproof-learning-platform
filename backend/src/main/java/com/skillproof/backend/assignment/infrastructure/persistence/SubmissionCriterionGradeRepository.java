package com.skillproof.backend.assignment.infrastructure.persistence;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionCriterionGradeRepository
    extends JpaRepository<SubmissionCriterionGrade, UUID> {
  List<SubmissionCriterionGrade> findBySubmissionId(UUID submissionId);
}
