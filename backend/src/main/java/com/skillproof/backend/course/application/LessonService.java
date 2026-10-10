package com.skillproof.backend.course.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.assignment.contract.AssignmentVersionBridge;
import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CourseAssessmentDependency;
import com.skillproof.backend.course.contract.CourseStructureQuery;
import com.skillproof.backend.course.infrastructure.CourseModuleJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseResourceEntity;
import com.skillproof.backend.course.infrastructure.CourseResourceJpaRepository;
import com.skillproof.backend.course.infrastructure.persistence.LessonEntity;
import com.skillproof.backend.course.infrastructure.persistence.LessonRepository;

@Service
public class LessonService {

    private final LessonRepository lessons;
    private final CourseModuleJpaRepository modules;
    private final CourseResourceJpaRepository resources;
    private final CourseStructureQuery structure;
    private final CourseContentService content;
    private final AssignmentVersionBridge assignments;
    private final CourseAssessmentDependency assessments;

    public LessonService(LessonRepository lessons, CourseModuleJpaRepository modules, CourseResourceJpaRepository resources,
            CourseStructureQuery structure, CourseContentService content, AssignmentVersionBridge assignments, CourseAssessmentDependency assessments) {
        this.lessons = lessons;
        this.modules = modules;
        this.resources = resources;
        this.structure = structure;
        this.content = content;
        this.assignments = assignments;
        this.assessments = assessments;
    }

    private LessonEntity lesson(UUID id) {
        return lessons.findById(id).orElseThrow(() -> new NotFoundException("LESSON_NOT_FOUND", "Lesson not found"));
    }

    private UUID version(UUID module) {
        return modules.findById(module).orElseThrow(() -> new NotFoundException("MODULE_NOT_FOUND", "Module not found")).getVersionId();
    }

    private void validate(int position, String title, String body) {
        if (position < 1 || title == null || title.isBlank() || title.length() > 180 || body == null || body.length() > 100000) {
            throw new BadRequestException("LESSON_INVALID", "Invalid lesson");
        }
    }

    @Transactional
    public LessonEntity create(UUID actor, UUID module, int position, String title, String body) {
        structure.requireDraftAuthor(actor, version(module));
        validate(position, title, body);
        return lessons.save(new LessonEntity(UUID.randomUUID(), module, position, title.trim(), body));
    }

    @Transactional
    public LessonEntity edit(UUID actor, UUID id, int position, String title, String body) {
        var lesson = lesson(id);
        structure.requireDraftAuthor(actor, version(lesson.getModuleId()));
        validate(position, title, body);
        lesson.edit(position, title.trim(), body);
        return lessons.save(lesson);
    }

    @Transactional
    public void delete(UUID actor, UUID id) {
        var lesson = lesson(id);
        structure.requireDraftAuthor(actor, version(lesson.getModuleId()));
        if (assignments.hasOwner("LESSON", id) || assessments.hasOwner("LESSON", id)) {
            throw new ConflictException("LESSON_ACTIVITIES", "Remove owned assignments and assessments first");
        }
        var ids = resources.findByLessonIdOrderByPosition(id).stream().map(CourseResourceEntity::getId).toList();
        for (UUID resource : ids) {
            content.deleteResource(actor, resource);
        }
        lessons.delete(lesson);
    }

    @Transactional
    public Map<String, Object> resource(UUID actor, UUID lessonId, int position, String kind, String title, String body, String url, boolean required, boolean preview) {
        var lesson = lesson(lessonId);
        structure.requireDraftAuthor(actor, version(lesson.getModuleId()));
        var result = content.resource(actor, lesson.getModuleId(), position, kind, title, body, url);
        var resource = resources.findById((UUID) result.get("id")).orElseThrow();
        resource.attachLesson(lessonId, required, preview);
        return result;
    }

    @Transactional(readOnly = true)
    public List<LessonEntity> authorList(UUID actor, UUID module) {
        structure.requireVersionAuthor(actor, version(module));
        return lessons.findByModuleIdOrderByPosition(module);
    }
}
