package com.skillproof.backend.assignment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface SubmissionCriterionGradeRepository
        extends JpaRepository<SubmissionCriterionGrade, UUID> {
    List<SubmissionCriterionGrade> findBySubmissionId(UUID submissionId);
}
