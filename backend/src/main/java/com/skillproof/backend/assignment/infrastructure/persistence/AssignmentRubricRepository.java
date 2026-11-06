package com.skillproof.backend.assignment.infrastructure.persistence;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRubricRepository extends JpaRepository<AssignmentRubricCriterion, UUID> {
  List<AssignmentRubricCriterion> findByAssignmentIdOrderByPosition(UUID assignmentId);

  void deleteByAssignmentId(UUID assignmentId);
}
