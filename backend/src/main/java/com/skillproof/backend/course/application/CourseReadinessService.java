package com.skillproof.backend.course.application;

import com.skillproof.backend.course.contract.*;
import com.skillproof.backend.course.infrastructure.*;
import com.skillproof.backend.course.infrastructure.persistence.LessonRepository;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CourseReadinessService {
    private final CourseAuthoringService authoring;
    private final CourseContextRepository versions;
    private final CourseModuleJpaRepository modules;
    private final CourseResourceJpaRepository resources;
    private final LessonRepository lessons;
    private final CompletionPolicyJpaRepository policies;
    private final CourseMediaDependency media;
    private final CourseAssessmentDependency assessments;

    public CourseReadinessService(
            CourseAuthoringService authoring,
            CourseContextRepository versions,
            CourseModuleJpaRepository modules,
            CourseResourceJpaRepository resources,
            LessonRepository lessons,
            CompletionPolicyJpaRepository policies,
            CourseMediaDependency media,
            CourseAssessmentDependency assessments) {
        this.authoring = authoring;
        this.versions = versions;
        this.modules = modules;
        this.resources = resources;
        this.lessons = lessons;
        this.policies = policies;
        this.media = media;
        this.assessments = assessments;
    }

    public Map<String, Object> check(UUID actor, UUID id) {
        authoring.outline(actor, id);
        var v = versions.findById(id).orElseThrow();
        List<Map<String, Object>> issues = new ArrayList<>();
        if (lessons.countByVersionId(id) == 0)
            issues.add(
                    Map.of(
                            "code",
                            "COURSE_EMPTY",
                            "tab",
                            "outline",
                            "message",
                            "Add at least one lesson"));
        for (var m : modules.findByVersionIdOrderByPosition(id)) {
            for (var l : lessons.findByModuleIdOrderByPosition(m.getId())) {
                var files = resources.findByLessonIdOrderByPosition(l.getId());
                if (l.getBody().isBlank() && files.isEmpty())
                    issues.add(
                            Map.of(
                                    "code",
                                    "LESSON_EMPTY",
                                    "tab",
                                    "outline",
                                    "targetId",
                                    l.getId(),
                                    "message",
                                    "Lesson needs text or a resource"));
            }
            for (var r : resources.findByModuleIdOrderByPosition(m.getId()))
                if ((Set.of("FILE", "AUDIO", "IMAGE").contains(r.getKind())
                                || ("VIDEO".equals(r.getKind()) && r.getUrl() == null))
                        && !media.hasMatchingAsset(r.getId(), r.getKind()))
                    issues.add(
                            Map.of(
                                    "code",
                                    "MEDIA_REQUIRED",
                                    "tab",
                                    "outline",
                                    "targetId",
                                    r.getId(),
                                    "message",
                                    "Upload a ready file for " + r.getTitle()));
        }
        if (assessments.hasDraftAssessments(id))
            issues.add(
                    Map.of(
                            "code",
                            "ASSESSMENT_DRAFT",
                            "tab",
                            "assessments",
                            "message",
                            "Publish or remove draft assessments"));
        if (policies.findById(id).orElseThrow().isRequireOfficialAssessments()
                && !assessments.hasPublishedOfficialAssessment(id))
            issues.add(
                    Map.of(
                            "code",
                            "OFFICIAL_REQUIRED",
                            "tab",
                            "assessments",
                            "message",
                            "Publish an official assessment"));
        return Map.of("ready", issues.isEmpty(), "status", v.getStatus(), "issues", issues);
    }
}
