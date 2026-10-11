package com.skillproof.backend.assignment.application;

import com.skillproof.backend.assignment.infrastructure.persistence.*;
import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.course.contract.CourseStructureQuery;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignmentRubricService {
  public record CriterionInput(String title, int weight) {}

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.course.contract.CourseWorkspaceQuery workspace;

  private final AssignmentRepository assignments;
  private final AssignmentRubricRepository rubric;
  private final CourseStructureQuery courses;

  public AssignmentRubricService(
      AssignmentRepository assignments,
      AssignmentRubricRepository rubric,
      CourseStructureQuery courses) {
    this.assignments = assignments;
    this.rubric = rubric;
    this.courses = courses;
  }

  @Transactional(readOnly = true)
  public List<AssignmentRubricCriterion> author(UUID actor, UUID id) {
    var a =
        assignments
            .findById(id)
            .orElseThrow(
                () -> new NotFoundException("ASSIGNMENT_NOT_FOUND", "Assignment not found"));
    try {
      workspace.requireGrader(actor, a.getVersionId());
    } catch (org.springframework.security.access.AccessDeniedException denied) {
      courses.requireVersionAuthor(actor, a.getVersionId());
    }
    return rubric.findByAssignmentIdOrderByPosition(id);
  }

  @Transactional(readOnly = true)
  public List<AssignmentRubricCriterion> learner(UUID actor, UUID id, UUID enrollment) {
    var a =
        assignments
            .findById(id)
            .orElseThrow(
                () -> new NotFoundException("ASSIGNMENT_NOT_FOUND", "Assignment not found"));
    courses.requireEnrollment(actor, enrollment, a.getVersionId(), false);
    return rubric.findByAssignmentIdOrderByPosition(id);
  }

  @Transactional
  public List<AssignmentRubricCriterion> save(UUID actor, UUID id, List<CriterionInput> rows) {
    var a =
        assignments
            .findById(id)
            .orElseThrow(
                () -> new NotFoundException("ASSIGNMENT_NOT_FOUND", "Assignment not found"));
    courses.requireDraftAuthor(actor, a.getVersionId());
    if (rows == null
        || rows.size() > 20
        || rows.stream().anyMatch(Objects::isNull)
        || (!rows.isEmpty() && rows.stream().mapToInt(CriterionInput::weight).sum() != 100)
        || rows.stream()
            .anyMatch(
                c ->
                    c.title() == null
                        || c.title().isBlank()
                        || c.title().length() > 180
                        || c.weight() < 1
                        || c.weight() > 100))
      throw new BadRequestException(
          "RUBRIC_INVALID",
          "Use up to 20 criteria with positive weights totaling 100; empty means"
              + " overall-score grading");
    rubric.deleteByAssignmentId(id);
    rubric.flush();
    List<AssignmentRubricCriterion> saved = new ArrayList<>();
    for (int i = 0; i < rows.size(); i++) {
      var c = rows.get(i);
      saved.add(
          new AssignmentRubricCriterion(
              UUID.randomUUID(), id, c.title().trim(), c.weight(), i + 1));
    }
    return rubric.saveAll(saved);
  }
}
