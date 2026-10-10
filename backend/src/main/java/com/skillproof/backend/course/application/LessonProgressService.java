package com.skillproof.backend.course.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CompletionEvidenceQuery;
import com.skillproof.backend.course.contract.CourseStructureQuery;
import com.skillproof.backend.course.infrastructure.CourseModuleJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseResourceEntity;
import com.skillproof.backend.course.infrastructure.CourseResourceJpaRepository;
import com.skillproof.backend.course.infrastructure.ResourceProgressId;
import com.skillproof.backend.course.infrastructure.ResourceProgressJpaRepository;
import com.skillproof.backend.course.infrastructure.persistence.LessonProgressEntity;
import com.skillproof.backend.course.infrastructure.persistence.LessonProgressRepository;
import com.skillproof.backend.course.infrastructure.persistence.LessonRepository;

@Service
public class LessonProgressService {

    private final LessonRepository lessons;
    private final LessonProgressRepository progress;
    private final CourseModuleJpaRepository modules;
    private final CourseResourceJpaRepository resources;
    private final ResourceProgressJpaRepository resourceProgress;
    private final CourseStructureQuery structure;
    private final CompletionEvidenceQuery completion;

    public LessonProgressService(LessonRepository lessons, LessonProgressRepository progress, CourseModuleJpaRepository modules,
            CourseResourceJpaRepository resources, ResourceProgressJpaRepository resourceProgress, CourseStructureQuery structure,
            CompletionEvidenceQuery completion) {
        this.lessons = lessons;
        this.progress = progress;
        this.modules = modules;
        this.resources = resources;
        this.resourceProgress = resourceProgress;
        this.structure = structure;
        this.completion = completion;
    }

    @Transactional
    public CompletionEvidenceQuery.Evidence complete(UUID learner, UUID enrollment, UUID lessonId) {
        var lesson = lessons.findById(lessonId).orElseThrow(() -> new NotFoundException("LESSON_NOT_FOUND", "Lesson not found"));
        UUID version = modules.findById(lesson.getModuleId()).orElseThrow().getVersionId();
        structure.requireEnrollment(learner, enrollment, version, true);
        boolean ready = resources.findByLessonIdOrderByPosition(lessonId).stream().filter(CourseResourceEntity::isRequiredForCompletion)
                .allMatch(r -> resourceProgress.existsById(new ResourceProgressId(enrollment, r.getId())));
        if (!ready) {
            throw new ConflictException("LESSON_RESOURCES_INCOMPLETE", "Complete the required lesson resources first");
        }
        if (!progress.existsByEnrollmentIdAndLessonId(enrollment, lessonId)) {
            progress.saveAndFlush(new LessonProgressEntity(UUID.randomUUID(), enrollment, lessonId, Instant.now()));
        }
        return completion.evaluate(enrollment, 0, 0);
    }
}
