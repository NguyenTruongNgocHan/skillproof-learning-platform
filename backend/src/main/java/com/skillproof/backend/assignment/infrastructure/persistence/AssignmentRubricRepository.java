package com.skillproof.backend.assignment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface AssignmentRubricRepository extends JpaRepository<AssignmentRubricCriterion, UUID> {
    List<AssignmentRubricCriterion> findByAssignmentIdOrderByPosition(UUID assignmentId);

    void deleteByAssignmentId(UUID assignmentId);
}
