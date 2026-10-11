package com.skillproof.backend.assignment.application;

import com.skillproof.backend.assignment.contract.AssignmentVersionBridge;
import com.skillproof.backend.assignment.infrastructure.persistence.*;
import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.course.contract.CourseStructureQuery;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class AssignmentAuthoringService implements AssignmentVersionBridge {

    @org.springframework.beans.factory.annotation.Autowired
    private AssignmentRubricRepository rubric;

    private final AssignmentRepository assignments;
    private final CourseStructureQuery courses;

    public AssignmentAuthoringService(
            AssignmentRepository assignments, CourseStructureQuery courses) {
        this.assignments = assignments;
        this.courses = courses;
    }

    @Transactional
    public AssignmentEntity create(
            UUID actor,
            UUID version,
            String scope,
            UUID owner,
            String title,
            String instructions,
            boolean required,
            int pass,
            int max,
            Instant due) {
        courses.requireDraftAuthor(actor, version);
        courses.validateOwner(version, scope, owner);
        if (pass < 1
                || pass > 100
                || max < 1
                || max > 20
                || title == null
                || title.isBlank()
                || title.length() > 180
                || instructions == null
                || instructions.isBlank()
                || instructions.length() > 20000
                || (due != null && !due.isAfter(Instant.now()))) {
            throw new BadRequestException("ASSIGNMENT_INVALID", "Invalid assignment policy");
        }
        return assignments.save(
                new AssignmentEntity(
                        UUID.randomUUID(),
                        version,
                        scope,
                        owner,
                        title.trim(),
                        instructions,
                        required,
                        pass,
                        max,
                        due));
    }

    @Transactional
    public AssignmentEntity edit(
            UUID actor,
            UUID id,
            String scope,
            UUID owner,
            String title,
            String instructions,
            boolean required,
            int pass,
            int max,
            Instant due) {
        var value = assignments.findById(id).orElseThrow();
        courses.requireDraftAuthor(actor, value.getVersionId());
        courses.validateOwner(value.getVersionId(), scope, owner);
        if (pass < 1
                || pass > 100
                || max < 1
                || max > 20
                || title == null
                || title.isBlank()
                || title.length() > 180
                || instructions == null
                || instructions.isBlank()
                || instructions.length() > 20000
                || (due != null && !due.isAfter(Instant.now())))
            throw new BadRequestException("ASSIGNMENT_INVALID", "Invalid assignment policy");
        value.edit(scope, owner, title.trim(), instructions, required, pass, max, due);
        return value;
    }

    @Transactional
    public void delete(UUID actor, UUID id) {
        var assignment =
                assignments
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "ASSIGNMENT_NOT_FOUND", "Assignment not found"));
        courses.requireDraftAuthor(actor, assignment.getVersionId());
        assignments.delete(assignment);
    }

    @Transactional(readOnly = true)
    public List<AssignmentEntity> authorList(UUID actor, UUID version) {
        courses.requireVersionAuthor(actor, version);
        return assignments.findByVersionId(version);
    }

    @Override
    public boolean hasOwner(String scope, UUID owner) {
        return assignments.existsByOwnerScopeAndOwnerId(scope, owner);
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void cloneVersion(
            UUID source, UUID target, Map<UUID, UUID> modules, Map<UUID, UUID> lessons) {
        for (var old : assignments.findByVersionId(source)) {
            UUID owner =
                    switch (old.getOwnerScope()) {
                        case "COURSE" -> target;
                        case "MODULE" -> modules.get(old.getOwnerId());
                        case "LESSON" -> lessons.get(old.getOwnerId());
                        default -> throw new IllegalStateException("Invalid activity owner");
                    };
            var cloned =
                    assignments.save(
                            new AssignmentEntity(
                                    UUID.randomUUID(),
                                    target,
                                    old.getOwnerScope(),
                                    owner,
                                    old.getTitle(),
                                    old.getInstructions(),
                                    old.getRequired(),
                                    old.getPassPercent(),
                                    old.getMaxSubmissions(),
                                    null));
            for (var criterion : rubric.findByAssignmentIdOrderByPosition(old.getId()))
                rubric.save(
                        new AssignmentRubricCriterion(
                                UUID.randomUUID(),
                                cloned.getId(),
                                criterion.getTitle(),
                                criterion.getWeight(),
                                criterion.getPosition()));
        }
    }
}
