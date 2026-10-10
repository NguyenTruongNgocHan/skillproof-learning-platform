package com.skillproof.backend.course.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CourseAssessmentDependency;
import com.skillproof.backend.course.contract.CourseMediaDependency;
import com.skillproof.backend.course.infrastructure.CourseContextRepository;
import com.skillproof.backend.course.infrastructure.CourseEntity;
import com.skillproof.backend.course.infrastructure.CourseModuleEntity;
import com.skillproof.backend.course.infrastructure.CourseModuleJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseResourceEntity;
import com.skillproof.backend.course.infrastructure.CourseResourceJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseVersionEntity;

@Service
@Transactional(readOnly = true)
public class CourseContentService {

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;
    private final CourseModuleJpaRepository modules;
    private final CourseResourceJpaRepository resources;
    private final CourseContextRepository versions;
    private final CourseAccess access;
    private final CourseMediaDependency media;
    private final CourseAssessmentDependency assessments;
    private final com.skillproof.backend.course.infrastructure.persistence.LessonRepository lessons;
    private final com.skillproof.backend.assignment.contract.AssignmentVersionBridge assignments;

    public CourseContentService(CourseModuleJpaRepository modules, CourseResourceJpaRepository resources, CourseContextRepository versions,
            CourseAccess access, CourseMediaDependency media, CourseAssessmentDependency assessments,
            com.skillproof.backend.course.infrastructure.persistence.LessonRepository lessons, com.skillproof.backend.assignment.contract.AssignmentVersionBridge assignments) {
        this.modules = modules;
        this.resources = resources;
        this.versions = versions;
        this.access = access;
        this.media = media;
        this.assessments = assessments;
        this.lessons = lessons;
        this.assignments = assignments;
    }

    private CourseVersionEntity draftVersion(UUID id) {
        var version = versions
                .findForUpdate(id)
                .orElseThrow(()
                        -> new NotFoundException(
                        "COURSE_VERSION_NOT_FOUND",
                        "Course version not found"
                )
                );
        entityManager.refresh(version, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (!"DRAFT".equals(version.getStatus())) {
            throw new com.skillproof.backend.common.exception.ConflictException(
                    "COURSE_VERSION_IMMUTABLE",
                    "Published learning versions cannot be edited"
            );
        }
        version.invalidateReview();
        return version;
    }

    private CourseEntity pathForVersion(UUID versionId) {
        return versions
                .findCourseByVersionId(versionId)
                .orElseThrow(()
                        -> new NotFoundException(
                        "COURSE_VERSION_NOT_FOUND",
                        "Course version not found"
                )
                );
    }

    private CourseModuleEntity moduleEntity(UUID id) {
        return modules
                .findById(id)
                .orElseThrow(()
                        -> new NotFoundException(
                        "COURSE_NOT_FOUND",
                        "Course module not found"
                )
                );
    }

    @Transactional
    public Map<String, Object> module(
            UUID actor,
            UUID version,
            int position,
            String title
    ) {
        var draft = draftVersion(version);
        var p = pathForVersion(draft.getId());
        access.manage(actor, p);
        var m = modules.save(
                new CourseModuleEntity(
                        UUID.randomUUID(),
                        version,
                        position,
                        title.trim()
                )
        );
        return Map.of(
                "id",
                m.getId(),
                "version_id",
                version,
                "position",
                position,
                "title",
                title.trim()
        );
    }

    @Transactional
    public Map<String, Object> updateModule(
            UUID actor,
            UUID id,
            int position,
            String title
    ) {
        var m = moduleEntity(id);
        var draft = draftVersion(m.getVersionId());
        var p = pathForVersion(draft.getId());
        access.manage(actor, p);
        m.edit(position, title.trim());
        return Map.of("id", id, "position", position, "title", title.trim());
    }

    @Transactional
    public void deleteModule(UUID actor, UUID id) {
        var m = moduleEntity(id);
        var draft = draftVersion(m.getVersionId());
        var p = pathForVersion(draft.getId());
        access.manage(actor, p);
        if (assignments.hasOwner("MODULE", id) || assessments.hasOwner("MODULE", id) || lessons.countByModuleId(id) > 0) {
            throw new com.skillproof.backend.common.exception.ConflictException("MODULE_NOT_EMPTY", "Remove lessons and owned activities first");
        }
        var ids = resources
                .findByModuleIdOrderByPosition(id)
                .stream()
                .map(CourseResourceEntity::getId)
                .toList();
        media.deleteForResources(ids);
        resources.deleteAllById(ids);
        modules.deleteById(id);
    }

    private boolean valid(String k, String b, String u) {
        if ("ARTICLE".equals(k)) {
            return b != null && !b.isBlank() && u == null;
        }
        return (("LINK".equals(k)
                || "VIDEO".equals(k)
                || "FILE".equals(k)
                || "AUDIO".equals(k) || "IMAGE".equals(k))
                && b == null
                && ("LINK".equals(k) || "VIDEO".equals(k)
                ? u != null && u.matches("https://.+")
                : u == null));
    }

    @Transactional
    public Map<String, Object> resource(
            UUID actor,
            UUID module,
            int position,
            String kind,
            String title,
            String body,
            String url
    ) {
        var m = moduleEntity(module);
        var draft = draftVersion(m.getVersionId());
        var p = pathForVersion(draft.getId());
        access.manage(actor, p);
        if (!valid(kind, body, url)) {
            throw new BadRequestException(
                    "RESOURCE_INVALID",
                    "Invalid resource"
            );
        }
        var r = resources.save(
                new CourseResourceEntity(
                        UUID.randomUUID(),
                        module,
                        position,
                        kind,
                        title.trim(),
                        body,
                        url
                )
        );
        return Map.of(
                "id",
                r.getId(),
                "module_id",
                module,
                "position",
                position,
                "kind",
                kind,
                "title",
                title.trim()
        );
    }

    @Transactional
    public Map<String, Object> updateResource(
            UUID actor,
            UUID id,
            int position,
            String kind,
            String title,
            String body,
            String url
    ) {
        var r = resources
                .findById(id)
                .orElseThrow(()
                        -> new NotFoundException(
                        "COURSE_NOT_FOUND",
                        "Resource not found"
                )
                );
        var m = moduleEntity(r.getModuleId());
        var draft = draftVersion(m.getVersionId());
        var p = pathForVersion(draft.getId());
        access.manage(actor, p);
        if (!valid(kind, body, url)) {
            throw new BadRequestException(
                    "RESOURCE_INVALID",
                    "Invalid resource"
            );
        }
        r.edit(position, kind, title.trim(), body, url);
        return Map.of(
                "id",
                id,
                "position",
                position,
                "kind",
                kind,
                "title",
                title.trim()
        );
    }

    @Transactional
    public void deleteResource(UUID actor, UUID id) {
        var r = resources
                .findById(id)
                .orElseThrow(()
                        -> new NotFoundException(
                        "COURSE_NOT_FOUND",
                        "Resource not found"
                )
                );
        var m = moduleEntity(r.getModuleId());
        var draft = draftVersion(m.getVersionId());
        access.manage(actor, pathForVersion(draft.getId()));
        media.deleteForResources(List.of(id));
        resources.deleteById(id);
    }
}
