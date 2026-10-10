package com.skillproof.backend.course.application;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.course.infrastructure.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns atomic ordering of draft content, including collision-free position
 * updates.
 */
@Service
public class CourseOrderingService {

    private final com.skillproof.backend.course.infrastructure.persistence.LessonRepository lessons;
    private final CourseContextRepository versions;
    private final CourseModuleJpaRepository modules;
    private final CourseResourceJpaRepository resources;
    private final CourseAccess access;

    public CourseOrderingService(
            CourseContextRepository versions,
            CourseModuleJpaRepository modules,
            CourseResourceJpaRepository resources,
            CourseAccess access, com.skillproof.backend.course.infrastructure.persistence.LessonRepository lessons
    ) {
        this.lessons = lessons;
        this.versions = versions;
        this.modules = modules;
        this.resources = resources;
        this.access = access;
    }

    private void authorize(UUID actor, UUID versionId) {
        var version = versions
                .findForUpdate(versionId)
                .orElseThrow(()
                        -> new NotFoundException(
                        "COURSE_VERSION_NOT_FOUND",
                        "Version not found"
                )
                );
        var path = versions.findCourseByVersionId(versionId).orElseThrow();
        access.manage(actor, path);
        version.invalidateReview();
        if (!"DRAFT".equals(version.getStatus())) {
            throw new ConflictException(
                    "COURSE_VERSION_IMMUTABLE",
                    "Reorder only draft content"
            );
        }
    }

    private void validate(List<UUID> supplied, List<UUID> existing) {
        if (supplied == null
                || supplied.size() != existing.size()
                || new HashSet<>(supplied).size() != supplied.size()
                || !new HashSet<>(supplied).equals(new HashSet<>(existing))) {
            throw new BadRequestException(
                    "INVALID_CONTENT_ORDER",
                    "Supply each current item exactly once"
            );
        }
    }

    @Transactional
    public void modules(UUID actor, UUID versionId, List<UUID> ids) {
        authorize(actor, versionId);
        var rows = modules.findByVersionIdOrderByPosition(versionId);
        validate(ids, rows.stream().map(CourseModuleEntity::getId).toList());
        int temporary = Math.addExact(
                rows
                        .stream()
                        .mapToInt(CourseModuleEntity::getPosition)
                        .max()
                        .orElse(0),
                1
        );
        var indexed = new HashMap<UUID, CourseModuleEntity>();
        for (var row : rows) {
            indexed.put(row.getId(), row);
            row.edit(temporary++, row.getTitle());
        }
        modules.flush();
        for (int i = 0; i < ids.size(); i++) {
            var row = indexed.get(ids.get(i));
            row.edit(i + 1, row.getTitle());
        }
        modules.flush();
    }

    @Transactional
    public void resources(UUID actor, UUID lessonId, List<UUID> ids) {
        var lesson = lessons.findById(lessonId).orElseThrow(() -> new NotFoundException("LESSON_NOT_FOUND", "Lesson not found"));
        var module = modules
                .findById(lesson.getModuleId())
                .orElseThrow(()
                        -> new NotFoundException(
                        "COURSE_MODULE_NOT_FOUND",
                        "Module not found"
                )
                );
        authorize(actor, module.getVersionId());
        var rows = resources.findByLessonIdOrderByPosition(lessonId);
        validate(
                ids,
                rows.stream().map(CourseResourceEntity::getId).toList()
        );
        int temporary = Math.addExact(
                rows
                        .stream()
                        .mapToInt(CourseResourceEntity::getPosition)
                        .max()
                        .orElse(0),
                1
        );
        var indexed = new HashMap<UUID, CourseResourceEntity>();
        for (var row : rows) {
            indexed.put(row.getId(), row);
            row.edit(
                    temporary++,
                    row.getKind(),
                    row.getTitle(),
                    row.getBody(),
                    row.getUrl()
            );
        }
        resources.flush();
        for (int i = 0; i < ids.size(); i++) {
            var row = indexed.get(ids.get(i));
            row.edit(
                    i + 1,
                    row.getKind(),
                    row.getTitle(),
                    row.getBody(),
                    row.getUrl()
            );
        }
        resources.flush();
    }

    @Transactional
    public void lessons(UUID actor, UUID moduleId, List<UUID> ids) {
        var module = modules.findById(moduleId).orElseThrow(() -> new NotFoundException("MODULE_NOT_FOUND", "Module not found"));
        authorize(actor, module.getVersionId());
        var rows = lessons.findByModuleIdOrderByPosition(moduleId);
        validate(ids, rows.stream().map(com.skillproof.backend.course.infrastructure.persistence.LessonEntity::getId).toList());
        int temporary = Math.addExact(rows.stream().mapToInt(com.skillproof.backend.course.infrastructure.persistence.LessonEntity::getPosition).max().orElse(0), 1);
        var indexed = new HashMap<UUID, com.skillproof.backend.course.infrastructure.persistence.LessonEntity>();
        for (var row : rows) {
            indexed.put(row.getId(), row);
            row.edit(temporary++, row.getTitle(), row.getBody());
        }
        lessons.flush();
        for (int i = 0; i < ids.size(); i++) {
            var row = indexed.get(ids.get(i));
            row.edit(i + 1, row.getTitle(), row.getBody());
        }
        lessons.flush();
    }

}
