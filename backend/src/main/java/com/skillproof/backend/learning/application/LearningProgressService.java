package com.skillproof.backend.learning.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.learning.infrastructure.EnrollmentEntity;
import com.skillproof.backend.learning.infrastructure.LearningContextRepository;
import com.skillproof.backend.learning.infrastructure.LearningEnrollmentRepository;
import com.skillproof.backend.learning.infrastructure.LearningPathJpaRepository;
import com.skillproof.backend.learning.infrastructure.LearningResourceContextRepository;
import com.skillproof.backend.learning.infrastructure.ResourceProgressEntity;
import com.skillproof.backend.learning.infrastructure.ResourceProgressJpaRepository;
import com.skillproof.backend.organization.contract.OrganizationPublicQuery;

@Service
public class LearningProgressService {

    private final LearningEnrollmentRepository enrollments;
    private final LearningResourceContextRepository resources;
    private final ResourceProgressJpaRepository progress;
    private final LearningAccess access;
    private final CompletionEvidenceService evidence;
    private final OrganizationPublicQuery organizations;

    public LearningProgressService(LearningEnrollmentRepository e, LearningResourceContextRepository r, ResourceProgressJpaRepository p, LearningAccess a, CompletionEvidenceService c, OrganizationPublicQuery o, LearningPathJpaRepository paths, LearningContextRepository versions) {
        enrollments = e;
        resources = r;
        progress = p;
        access = a;
        evidence = c;
        organizations = o;
        this.paths = paths;
        this.versions = versions;
    }

    private EnrollmentEntity enrollmentEntity(UUID l, UUID id) {
        access.learner(l);
        return enrollments.findByIdAndLearnerId(id, l).orElseThrow(() -> new NotFoundException("LEARNING_NOT_FOUND", "Enrollment not found"));
    }
    private final LearningPathJpaRepository paths;
    private final LearningContextRepository versions;

    public List<Map<String, Object>> discover() {
        return paths.findAll().stream().flatMap(p -> versions.findFirstByPathIdAndStatusOrderByVersionNoDesc(p.getId(), "PUBLISHED").stream().filter(v -> organizations.find(p.getOrganizationId()).map(OrganizationPublicQuery.OrganizationView::approved).orElse(false)).map(v -> Map.<String, Object>of("id", p.getId(), "slug", p.getSlug(), "title", p.getTitle(), "summary", p.getSummary(), "organization_id", p.getOrganizationId(), "version_id", v.getId(), "version_no", v.getVersionNo()))).toList();
    }

    public Map<String, Object> publicDetail(UUID id) {
        var p = paths.findById(id).orElseThrow(() -> new NotFoundException("LEARNING_NOT_FOUND", "Published learning path not found"));
        var v = versions.findFirstByPathIdAndStatusOrderByVersionNoDesc(id, "PUBLISHED").orElseThrow(() -> new NotFoundException("LEARNING_NOT_FOUND", "Published learning path not found"));
        return Map.of("id", p.getId(), "title", p.getTitle(), "summary", p.getSummary(), "organization_id", p.getOrganizationId(), "version_id", v.getId(), "version_no", v.getVersionNo());
    }

    @Transactional
    public Map<String, Object> enroll(UUID learner, UUID pathId) {
        access.learner(learner);
        var p = publicDetail(pathId);
        var e = new EnrollmentEntity(UUID.randomUUID(), learner, pathId, (UUID) p.get("version_id"), "ACTIVE", Instant.now());
        return enrollmentMap(enrollments.save(e));
    }

    private Map<String, Object> enrollmentMap(EnrollmentEntity e) {
        return Map.of("id", e.getId(), "learner_id", e.getLearnerId(), "version_id", e.getVersionId(), "status", e.getStatus());
    }

    public List<Map<String, Object>> mine(UUID learner) {
        access.learner(learner);
        return enrollments.findByLearnerIdOrderByEnrolledAtDesc(learner).stream().map(this::enrollmentMap).toList();
    }

    public Map<String, Object> enrollment(UUID learner, UUID id) {
        var e = enrollmentEntity(learner, id);
        return Map.of("id", e.getId(), "learner_id", e.getLearnerId(), "version_id", e.getVersionId(), "status", e.getStatus());
    }

    public Map<String, Object> course(UUID learner, UUID id) {
        return Map.of("enrollment", enrollment(learner, id), "progress", progress(learner, id));
    }

    public Map<String, Object> progress(UUID learner, UUID id) {
        var e = enrollmentEntity(learner, id);
        long done = progress.countByEnrollmentId(id), total = resources.countByVersionId(e.getVersionId());
        return Map.of("totalResources", total, "completedResources", done, "officialAssessments", 0, "passedAssessments", 0, "completed", "COMPLETED".equals(e.getStatus()));
    }

    @Transactional
    public Map<String, Object> complete(UUID learner, UUID enrollmentId, UUID resource) {
        var e = enrollmentEntity(learner, enrollmentId);
        if (resources.findById(resource).isEmpty()) {
            throw new NotFoundException("RESOURCE_NOT_IN_ENROLLMENT", "Resource does not belong to this enrollment");
        
        }progress.save(new ResourceProgressEntity(enrollmentId, resource, Instant.now()));
        evidence.evaluate(enrollmentId, 0, 0);
        return progress(learner, enrollmentId);
    }

    public void evaluate(UUID id, int required, int passed) {
        evidence.evaluate(id, required, passed);
    }
}
