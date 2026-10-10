package com.skillproof.backend.learningpath.application;

import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import com.skillproof.backend.learningpath.domain.PathRules;
import com.skillproof.backend.learningpath.infrastructure.persistence.*;
import com.skillproof.backend.course.contract.CoursePlanQuery;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.common.exception.*;

@Service
public class LearningPathService {

    private final LearningPathRepository paths;
    private final LearningPathItemRepository items;
    private final CoursePlanQuery courses;
    private final IdentityAccessQuery identities;

    public LearningPathService(LearningPathRepository paths, LearningPathItemRepository items, CoursePlanQuery courses, IdentityAccessQuery identities) {
        this.paths = paths;
        this.items = items;
        this.courses = courses;
        this.identities = identities;
    }

    private void learner(UUID actor) {
        if (!identities.isActiveLearner(actor)) {
            throw new AccessDeniedException("Active learner required");
    
        }}

    @Transactional
    public LearningPathEntity create(UUID actor, String title, String goal) {
        learner(actor);
        if (title == null || title.isBlank() || title.length() > 180 || goal == null || goal.length() > 1000) {
            throw new BadRequestException("PATH_INVALID", "Invalid path goal or title");
        }
        return paths.save(new LearningPathEntity(UUID.randomUUID(), actor, title.trim(), goal, Instant.now()));
    }

    @Transactional
    public LearningPathItemEntity add(UUID actor, UUID path, UUID version, int position, UUID prerequisite) {
        learner(actor);
        paths.lock(path, actor).orElseThrow(() -> new NotFoundException("PATH_NOT_FOUND", "Path not found"));
        var course = courses.item(actor, version);
        var rows = items.findByPathIdOrderByPosition(path);
        if (rows.size() >= 100) {
            throw new BadRequestException("PATH_LIMIT", "A path supports up to 100 courses");
        }
        if (rows.stream().anyMatch(i -> courses.item(actor, i.getCourseVersionId()).courseId().equals(course.courseId()))) {
            throw new ConflictException("PATH_COURSE_DUPLICATE", "Course already belongs to this path");
        }
        Integer before = prerequisite == null ? null : rows.stream().filter(i -> i.getId().equals(prerequisite)).map(LearningPathItemEntity::getPosition).findFirst()
                .orElseThrow(() -> new BadRequestException("PATH_PREREQUISITE", "Prerequisite must belong to this path"));
        if (!PathRules.validPrerequisite(position, before)) {
            throw new BadRequestException("PATH_ORDER", "Prerequisite must precede this course");
        }
        return items.save(new LearningPathItemEntity(UUID.randomUUID(), path, version, position, prerequisite));
    }

    @Transactional
    public void remove(UUID actor, UUID path, UUID item) {
        learner(actor);
        paths.lock(path, actor).orElseThrow(() -> new NotFoundException("PATH_NOT_FOUND", "Path not found"));
        var row = items.findById(item).filter(i -> path.equals(i.getPathId())).orElseThrow(() -> new NotFoundException("PATH_ITEM_NOT_FOUND", "Item not found"));
        if (items.existsByPrerequisiteItemId(item)) {
            throw new ConflictException("PATH_PREREQUISITE_USED", "Remove dependent path items first");
        
        }items.delete(row);
    }

    public record Item(UUID id, int position, UUID prerequisiteItemId, CoursePlanQuery.CourseItem course, boolean ready) {

    }

    @Transactional(readOnly = true)
    public List<Item> outline(UUID actor, UUID path) {
        learner(actor);
        paths.findById(path).filter(p -> actor.equals(p.getLearnerId())).orElseThrow(() -> new NotFoundException("PATH_NOT_FOUND", "Path not found"));
        var rows = items.findByPathIdOrderByPosition(path);
        var completion = new HashMap<UUID, Boolean>();
        var result = new ArrayList<Item>();
        for (var row : rows) {
            var course = courses.item(actor, row.getCourseVersionId());
            boolean ready = row.getPrerequisiteItemId() == null || Boolean.TRUE.equals(completion.get(row.getPrerequisiteItemId()));
            result.add(new Item(row.getId(), row.getPosition(), row.getPrerequisiteItemId(), course, ready));
            completion.put(row.getId(), course.completed());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<LearningPathEntity> mine(UUID actor) {
        learner(actor);
        return paths.findByLearnerIdOrderByCreatedAtDesc(actor);
    }
}
