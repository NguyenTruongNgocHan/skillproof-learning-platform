package com.skillproof.backend.course.application;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CourseAssessmentDependency;
import com.skillproof.backend.course.contract.CourseMediaDependency;
import com.skillproof.backend.course.infrastructure.CompletionPolicyEntity;
import com.skillproof.backend.course.infrastructure.CompletionPolicyJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseContextRepository;
import com.skillproof.backend.course.infrastructure.CourseModuleEntity;
import com.skillproof.backend.course.infrastructure.CourseModuleJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseEntity;
import com.skillproof.backend.course.infrastructure.CourseJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseVersionEntity;
import com.skillproof.backend.course.infrastructure.CourseResourceEntity;
import com.skillproof.backend.course.infrastructure.CourseResourceJpaRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CourseAuthoringService {

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    private final com.skillproof.backend.course.infrastructure.persistence.LessonRepository lessons;
    private final com.skillproof.backend.assignment.contract.AssignmentVersionBridge assignments;
    private final CourseJpaRepository paths;
    private final CourseModuleJpaRepository modules;
    private final CourseResourceJpaRepository resources;
    private final CourseContextRepository versions;
    private final CompletionPolicyJpaRepository policies;
    private final CourseAccess access;
    private final CourseMediaDependency media;
    private final CourseAssessmentDependency assessments;

    public CourseAuthoringService(
            CourseJpaRepository p,
            CourseModuleJpaRepository m,
            CourseResourceJpaRepository r,
            CourseContextRepository v,
            CompletionPolicyJpaRepository cp,
            CourseAccess a,
            CourseMediaDependency md,
            CourseAssessmentDependency ad, com.skillproof.backend.course.infrastructure.persistence.LessonRepository lessons, com.skillproof.backend.assignment.contract.AssignmentVersionBridge assignments
    ) {
        paths = p;
        this.lessons = lessons;
        this.assignments = assignments;
        modules = m;
        resources = r;
        versions = v;
        policies = cp;
        access = a;
        media = md;
        assessments = ad;
    }

    private CourseEntity path(UUID id) {
        return paths
                .findById(id)
                .orElseThrow(()
                        -> new NotFoundException(
                        "COURSE_NOT_FOUND",
                        "Course record not found"
                )
                );
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
                    "Published course versions cannot be edited"
            );
        }
        version.invalidateReview();
        return version;
    }

    public List<Map<String, Object>> owned(UUID actor, UUID org) {
        access.organizer(actor, org);
        return paths
                .findByOrganizationIdOrderByCreatedAtDesc(org)
                .stream()
                .map(p
                        -> Map.<String, Object>of(
                        "id",
                        p.getId(),
                        "organization_id",
                        p.getOrganizationId(),
                        "slug",
                        p.getSlug(),
                        "title",
                        p.getTitle(),
                        "summary",
                        p.getSummary()
                )
                )
                .toList();
    }

    @Transactional
    public Map<String, Object> create(
            UUID actor,
            UUID org,
            String slug,
            String title,
            String summary
    ) {
        if (org == null) {
            access.learner(actor);
        } else {
            access.organizer(actor, org);
        }
        var p = paths.save(
                new CourseEntity(
                        UUID.randomUUID(),
                        org,
                        slug.toLowerCase(java.util.Locale.ROOT),
                        title.trim(),
                        summary.trim(),
                        actor,
                        Instant.now()
                )
        );
        var version = versions.save(
                new CourseVersionEntity(
                        UUID.randomUUID(),
                        p.getId(),
                        1,
                        "DRAFT",
                        Instant.now()
                )
        );
        policies.save(new CompletionPolicyEntity(version.getId(), true, false));
        return Map.of(
                "id",
                p.getId(),
                "owner_id",
                actor,
                "slug",
                p.getSlug(),
                "title",
                p.getTitle(),
                "summary",
                p.getSummary()
        );
    }

    @Transactional
    public Map<String, Object> updateCourse(
            UUID actor,
            UUID id,
            String title,
            String summary
    ) {
        var p = path(id);
        access.manage(actor, p);
        paths.findForUpdate(id).orElseThrow();
        var draft = versions.findFirstByCourseIdAndStatusOrderByVersionNoDesc(id, "DRAFT").orElseThrow(() -> new com.skillproof.backend.common.exception.ConflictException("DRAFT_REQUIRED", "Create a draft before editing metadata"));
        draft.invalidateReview();
        p.edit(title.trim(), summary.trim());
        return Map.of(
                "id",
                p.getId(),
                "title",
                p.getTitle(),
                "summary",
                p.getSummary()
        );
    }

    @Transactional
    public Map<String, Object> cloneVersion(UUID a, UUID p) {
        var path = paths
                .findForUpdate(p)
                .orElseThrow(()
                        -> new NotFoundException("COURSE_NOT_FOUND", "Path not found")
                );
        access.manage(a, path);
        var source = versions
                .findFirstByCourseIdAndStatusOrderByVersionNoDesc(p, "PUBLISHED")
                .orElseThrow(()
                        -> new NotFoundException(
                        "COURSE_VERSION_NOT_FOUND",
                        "No published version to clone"
                )
                );
        var existing = versions.findFirstByCourseIdAndStatusOrderByVersionNoDesc(
                p,
                "DRAFT"
        );
        if (existing.isPresent()) {
            return versionResponse(existing.get());
        }
        int next
                = versions
                        .findByCourseIdOrderByVersionNoDesc(p)
                        .stream()
                        .mapToInt(CourseVersionEntity::getVersionNo)
                        .max()
                        .orElse(0) + 1;
        var draft = versions.save(
                new CourseVersionEntity(
                        UUID.randomUUID(),
                        p,
                        next,
                        "DRAFT",
                        Instant.now()
                )
        );
        var sourcePolicy = policies.findById(source.getId()).orElseThrow();
        var draftPolicy = new CompletionPolicyEntity(
                draft.getId(),
                sourcePolicy.isRequireAllResources(),
                sourcePolicy.isRequireOfficialAssessments()
        );
        policies.save(draftPolicy);
        draft.configureOffer(source.getAccessMode(), source.getPriceVnd(), source.getCertificationPriceVnd());
        var resourceMapping = new HashMap<UUID, UUID>();
        var moduleMapping = new HashMap<UUID, UUID>();
        var lessonMapping = new HashMap<UUID, UUID>();
        for (var sourceModule : modules.findByVersionIdOrderByPosition(
                source.getId()
        )) {
            var draftModule = modules.save(
                    new CourseModuleEntity(
                            UUID.randomUUID(),
                            draft.getId(),
                            sourceModule.getPosition(),
                            sourceModule.getTitle()
                    )
            );
            moduleMapping.put(sourceModule.getId(), draftModule.getId());
            for (var oldLesson : lessons.findByModuleIdOrderByPosition(sourceModule.getId())) {
                var copy = lessons.save(new com.skillproof.backend.course.infrastructure.persistence.LessonEntity(UUID.randomUUID(), draftModule.getId(), oldLesson.getPosition(), oldLesson.getTitle(), oldLesson.getBody()));
                lessonMapping.put(oldLesson.getId(), copy.getId());
            }
            for (var sourceResource : resources.findByModuleIdOrderByPosition(
                    sourceModule.getId()
            )) {
                var cloned = resources.save(
                        new CourseResourceEntity(
                                UUID.randomUUID(),
                                draftModule.getId(),
                                sourceResource.getPosition(),
                                sourceResource.getKind(),
                                sourceResource.getTitle(),
                                sourceResource.getBody(),
                                sourceResource.getUrl()
                        )
                );
                cloned.attachLesson(lessonMapping.get(sourceResource.getLessonId()), sourceResource.isRequiredForCompletion(), sourceResource.isPreview());
                resourceMapping.put(sourceResource.getId(), cloned.getId());
            }
        }
        assessments.clonePublishedAssessments(source.getId(), draft.getId(), moduleMapping, lessonMapping);
        assignments.cloneVersion(source.getId(), draft.getId(), moduleMapping, lessonMapping);
        media.cloneResourceAssets(resourceMapping);
        return versionResponse(draft);
    }

    private Map<String, Object> versionResponse(CourseVersionEntity v) {
        return Map.of(
                "id",
                v.getId(),
                "course_id",
                v.getCourseId(),
                "version_no",
                v.getVersionNo(),
                "status",
                v.getStatus()
        );
    }

    @Transactional
    public Map<String, Object> policy(UUID a, UUID i, boolean r, boolean q) {
        var v = draftVersion(i);
        if (!access.canReview(a)) {
            access.manage(a, path(v.getCourseId()));
        }
        if (!r && !q) {
            throw new BadRequestException(
                    "COMPLETION_POLICY_EMPTY",
                    "Select at least one completion requirement"
            );
        }
        var p = policies.findById(i).orElseThrow();
        p.configure(r, q);
        policies.save(p);
        return Map.of(
                "versionId",
                i,
                "requireAllResources",
                r,
                "requireOfficialAssessments",
                q
        );
    }

    @Transactional
    public Map<String, Object> publish(UUID a, UUID i) {
        var context = versions
                .findById(i)
                .orElseThrow(()
                        -> new NotFoundException(
                        "COURSE_VERSION_NOT_FOUND",
                        "Course version not found"
                )
                );
        paths.findForUpdate(context.getCourseId()).orElseThrow();
        var v = versions.findForUpdate(i).orElseThrow();
        entityManager.refresh(v, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (!"DRAFT".equals(v.getStatus())) {
            throw new com.skillproof.backend.common.exception.ConflictException("VERSION_IMMUTABLE", "Publish only a draft");
        }
        var course = path(v.getCourseId());
        access.manage(a, course);
        if (course.getOrganizationId() == null && !"APPROVED".equals(v.getReviewStatus())) {
            throw new com.skillproof.backend.common.exception.ConflictException("COURSE_REVIEW_REQUIRED", "Community courses require administrator approval");
        }
        var resourceRows = modules
                .findByVersionIdOrderByPosition(i)
                .stream()
                .flatMap(module
                        -> resources.findByModuleIdOrderByPosition(module.getId()).stream()
                )
                .toList();
        if (resourceRows.stream().anyMatch(r -> r.getLessonId() == null)) {
            throw new BadRequestException("LESSON_REQUIRED", "Every resource must belong to a lesson");
        }
        if (lessons.countByVersionId(i) == 0) {
            throw new BadRequestException(
                    "COURSE_EMPTY",
                    "Add at least one lesson before publishing"
            );
        }
        var mediaResources = resourceRows
                .stream()
                .filter(
                        resource
                        -> "FILE".equals(resource.getKind())
                        || "AUDIO".equals(resource.getKind()) || "IMAGE".equals(resource.getKind())
                )
                .map(CourseResourceEntity::getId)
                .toList();
        if (!media.hasAssetsForAll(mediaResources)) {
            throw new BadRequestException(
                    "COURSE_MEDIA_NOT_READY",
                    "Every FILE, AUDIO and IMAGE resource must have an attached media asset before publishing"
            );
        }
        if (v.getStatus().equals("PUBLISHED")) {
            throw new com.skillproof.backend.common.exception.ConflictException(
                    "COURSE_VERSION_IMMUTABLE",
                    "Published course versions cannot be edited"
            );
        }
        if (assessments.hasDraftAssessments(i)) {
            throw new BadRequestException(
                    "COURSE_ASSESSMENT_DRAFT",
                    "Publish or remove all draft assessments before publishing the course version"
            );
        }
        if (policies.findById(i).orElseThrow().isRequireOfficialAssessments()
                && !assessments.hasPublishedOfficialAssessment(i)) {
            throw new BadRequestException(
                    "COURSE_ASSESSMENT_NOT_READY",
                    "An official assessment is required before publishing"
            );
        }
        versions
                .findFirstByCourseIdAndStatusOrderByVersionNoDesc(
                        v.getCourseId(),
                        "PUBLISHED"
                )
                .ifPresent(previous -> previous.archive());
        versions.flush();
        v.snapshot(course.getTitle(), course.getSummary());
        v.publish();
        versions.saveAndFlush(v);
        return Map.of("id", i, "status", "PUBLISHED");
    }

    public Map<String, Object> outline(UUID a, UUID i) {
        var v = versions
                .findById(i)
                .orElseThrow(()
                        -> new NotFoundException(
                        "COURSE_VERSION_NOT_FOUND",
                        "Course version not found"
                )
                );
        if (!access.canReview(a)) {
            access.manage(a, path(v.getCourseId()));
        }
        var policy = policies
                .findById(i)
                .orElseThrow(()
                        -> new NotFoundException(
                        "COMPLETION_POLICY_NOT_FOUND",
                        "Completion policy not found"
                )
                );
        var moduleRows = modules
                .findByVersionIdOrderByPosition(i)
                .stream()
                .map(module
                        -> Map.<String, Object>of(
                        "id",
                        module.getId(),
                        "version_id",
                        module.getVersionId(),
                        "position",
                        module.getPosition(),
                        "title",
                        module.getTitle(),
                        "lessons", lessons.findByModuleIdOrderByPosition(module.getId()),
                        "resources",
                        resources
                                .findByModuleIdOrderByPosition(module.getId())
                                .stream()
                                .map(resource -> {
                                    var row = new java.util.LinkedHashMap<
                                String, Object>();
                                    row.put("id", resource.getId());
                                    row.put("module_id", resource.getModuleId());
                                    row.put("lesson_id", resource.getLessonId());
                                    row.put("required", resource.isRequiredForCompletion());
                                    row.put("preview", resource.isPreview());
                                    row.put("position", resource.getPosition());
                                    row.put("kind", resource.getKind());
                                    row.put("title", resource.getTitle());
                                    row.put("body", resource.getBody());
                                    row.put("url", resource.getUrl());
                                    return row;
                                })
                                .toList()
                )
                )
                .toList();
        return Map.of(
                "version",
                Map.of(
                        "id",
                        v.getId(),
                        "course_id",
                        v.getCourseId(),
                        "version_no",
                        v.getVersionNo(),
                        "status",
                        v.getStatus()
                ),
                "modules",
                moduleRows,
                "policy",
                Map.of(
                        "require_all_resources",
                        policy.isRequireAllResources(),
                        "require_official_assessments",
                        policy.isRequireOfficialAssessments()
                )
        );
    }

    public List<Map<String, Object>> versions(UUID a, UUID p) {
        var path = path(p);
        access.manage(a, path);
        return versions
                .findByCourseIdOrderByVersionNoDesc(p)
                .stream()
                .map(v
                        -> Map.<String, Object>of(
                        "id",
                        v.getId(),
                        "course_id",
                        v.getCourseId(),
                        "version_no",
                        v.getVersionNo(),
                        "status",
                        v.getStatus()
                )
                )
                .toList();
    }

    public List<Map<String, Object>> contributed(UUID actor) {
        access.learner(actor);
        return paths.findByCreatedByOrderByCreatedAtDesc(actor).stream().filter(c -> c.getOrganizationId() == null)
                .map(c -> Map.<String, Object>of("id", c.getId(), "title", c.getTitle(), "summary", c.getSummary())).toList();
    }

    @Transactional
    public Map<String, Object> configureOffer(UUID actor, UUID versionId, String mode, long price, long certificationPrice) {
        var version = draftVersion(versionId);
        var course = path(version.getCourseId());
        access.manage(actor, course);
        if (!List.of("PUBLIC", "RESTRICTED").contains(mode) || price < 0 || certificationPrice < 0
                || price > 1_000_000_000L || certificationPrice > 1_000_000_000L
                || (price > 0 && price < 5000) || (certificationPrice > 0 && certificationPrice < 5000)
                || (course.getOrganizationId() == null && certificationPrice != 0)
                || ("RESTRICTED".equals(mode) && (price != 0 || certificationPrice != 0))) {
            throw new BadRequestException("OFFER_INVALID", "Use PUBLIC prices of zero or 5000–1000000000 VND, or RESTRICTED with zero prices");
        }
        version.configureOffer(mode, price, certificationPrice);
        return Map.of("versionId", versionId, "accessMode", mode, "priceVnd", price, "certificationPriceVnd", certificationPrice);
    }

    @Transactional
    public Map<String, Object> submitReview(UUID actor, UUID versionId) {
        var version = draftVersion(versionId);
        var course = path(version.getCourseId());
        access.manage(actor, course);
        if (course.getOrganizationId() != null) {
            throw new BadRequestException("COURSE_REVIEW", "Organization content uses organization approval");
        }
        version.submitReview();
        return Map.of("versionId", versionId, "reviewStatus", version.getReviewStatus());
    }

    @Transactional
    public Map<String, Object> review(UUID admin, UUID versionId, boolean approved, String reason) {
        access.admin(admin);
        var version = versions.findForUpdate(versionId).orElseThrow(() -> new NotFoundException("VERSION_NOT_FOUND", "Version not found"));
        if (!"DRAFT".equals(version.getStatus()) || !"SUBMITTED".equals(version.getReviewStatus())) {
            throw new com.skillproof.backend.common.exception.ConflictException("COURSE_REVIEW_STATE", "A submitted draft is required");
        }
        if (reason == null || reason.isBlank() || reason.length() > 1000) {
            throw new BadRequestException("REVIEW_REASON", "Provide a review reason");
        }
        version.review(approved, reason.trim());
        return Map.of("versionId", versionId, "reviewStatus", version.getReviewStatus());
    }

    public Map<String, Object> reviewOutline(UUID admin, UUID versionId) {
        access.admin(admin);
        var version = versions.findById(versionId).orElseThrow();
        var course = path(version.getCourseId());
        return outline(admin, versionId);
    }

    @Transactional(readOnly = true)
    public java.util.List<CourseVersionEntity> reviewQueue(UUID admin, int page, int size) {
        access.admin(admin);
        return versions.findByStatusAndReviewStatusOrderByCreatedAtAsc("DRAFT", "SUBMITTED", org.springframework.data.domain.PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
    }

}
