package com.skillproof.backend.learning.application;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.learning.contract.LearningAssessmentDependency;
import com.skillproof.backend.learning.contract.LearningMediaDependency;
import com.skillproof.backend.learning.infrastructure.CompletionPolicyEntity;
import com.skillproof.backend.learning.infrastructure.CompletionPolicyJpaRepository;
import com.skillproof.backend.learning.infrastructure.LearningContextRepository;
import com.skillproof.backend.learning.infrastructure.LearningModuleEntity;
import com.skillproof.backend.learning.infrastructure.LearningModuleJpaRepository;
import com.skillproof.backend.learning.infrastructure.LearningPathEntity;
import com.skillproof.backend.learning.infrastructure.LearningPathJpaRepository;
import com.skillproof.backend.learning.infrastructure.LearningPathVersionEntity;
import com.skillproof.backend.learning.infrastructure.LearningResourceEntity;
import com.skillproof.backend.learning.infrastructure.LearningResourceJpaRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LearningService {

    private final LearningPathJpaRepository paths;
    private final LearningModuleJpaRepository modules;
    private final LearningResourceJpaRepository resources;
    private final LearningContextRepository versions;
    private final CompletionPolicyJpaRepository policies;
    private final LearningAccess access;
    private final LearningMediaDependency media;
    private final LearningAssessmentDependency assessments;

    public LearningService(
        LearningPathJpaRepository p,
        LearningModuleJpaRepository m,
        LearningResourceJpaRepository r,
        LearningContextRepository v,
        CompletionPolicyJpaRepository cp,
        LearningAccess a,
        LearningMediaDependency md,
        LearningAssessmentDependency ad
    ) {
        paths = p;
        modules = m;
        resources = r;
        versions = v;
        policies = cp;
        access = a;
        media = md;
        assessments = ad;
    }

    private LearningPathEntity path(UUID id) {
        return paths
            .findById(id)
            .orElseThrow(() ->
                new NotFoundException(
                    "LEARNING_NOT_FOUND",
                    "Learning record not found"
                )
            );
    }

    private LearningPathVersionEntity draftVersion(UUID id) {
        var version = versions
            .findForUpdate(id)
            .orElseThrow(() ->
                new NotFoundException(
                    "LEARNING_VERSION_NOT_FOUND",
                    "Learning version not found"
                )
            );
        if (!"DRAFT".equals(version.getStatus())) {
            throw new com.skillproof.backend.common.exception.ConflictException(
                "LEARNING_VERSION_IMMUTABLE",
                "Published learning versions cannot be edited"
            );
        }
        return version;
    }

    private LearningPathEntity pathForVersion(UUID versionId) {
        return versions
            .findPathByVersionId(versionId)
            .orElseThrow(() ->
                new NotFoundException(
                    "LEARNING_VERSION_NOT_FOUND",
                    "Learning version not found"
                )
            );
    }

    public List<Map<String, Object>> owned(UUID actor, UUID org) {
        access.organizer(actor, org);
        return paths
            .findByOrganizationIdOrderByCreatedAtDesc(org)
            .stream()
            .map(p ->
                Map.<String, Object>of(
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
        access.organizer(actor, org);
        var p = paths.save(
            new LearningPathEntity(
                UUID.randomUUID(),
                org,
                slug.toLowerCase(),
                title.trim(),
                summary.trim(),
                actor,
                Instant.now()
            )
        );
        var version = versions.save(
            new LearningPathVersionEntity(
                UUID.randomUUID(),
                p.getId(),
                1,
                "DRAFT",
                Instant.now()
            )
        );
        policies.save(new CompletionPolicyEntity(version.getId(), true, true));
        return Map.of(
            "id",
            p.getId(),
            "organization_id",
            org,
            "slug",
            p.getSlug(),
            "title",
            p.getTitle(),
            "summary",
            p.getSummary()
        );
    }

    @Transactional
    public Map<String, Object> updatePath(
        UUID actor,
        UUID id,
        String title,
        String summary
    ) {
        var p = path(id);
        access.organizer(actor, p.getOrganizationId());
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

    private LearningModuleEntity moduleEntity(UUID id) {
        return modules
            .findById(id)
            .orElseThrow(() ->
                new NotFoundException(
                    "LEARNING_NOT_FOUND",
                    "Learning module not found"
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
        access.organizer(actor, p.getOrganizationId());
        var m = modules.save(
            new LearningModuleEntity(
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
        access.organizer(actor, p.getOrganizationId());
        m.edit(position, title.trim());
        return Map.of("id", id, "position", position, "title", title.trim());
    }

    @Transactional
    public void deleteModule(UUID actor, UUID id) {
        var m = moduleEntity(id);
        var draft = draftVersion(m.getVersionId());
        var p = pathForVersion(draft.getId());
        access.organizer(actor, p.getOrganizationId());
        var ids = resources
            .findByModuleIdOrderByPosition(id)
            .stream()
            .map(LearningResourceEntity::getId)
            .toList();
        media.deleteForResources(ids);
        resources.deleteAllById(ids);
        modules.deleteById(id);
    }

    private boolean valid(String k, String b, String u) {
        if ("ARTICLE".equals(k)) return b != null && !b.isBlank() && u == null;
        return (
            ("LINK".equals(k) ||
                "VIDEO".equals(k) ||
                "FILE".equals(k) ||
                "AUDIO".equals(k)) &&
            b == null &&
            ("LINK".equals(k) || "VIDEO".equals(k)
                ? u != null && u.matches("https://.+")
                : u == null || !u.isBlank())
        );
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
        access.organizer(actor, p.getOrganizationId());
        if (!valid(kind, body, url)) {
            throw new BadRequestException(
                "RESOURCE_INVALID",
                "Invalid resource"
            );
        }
        var r = resources.save(
            new LearningResourceEntity(
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
            .orElseThrow(() ->
                new NotFoundException(
                    "LEARNING_NOT_FOUND",
                    "Resource not found"
                )
            );
        var m = moduleEntity(r.getModuleId());
        var draft = draftVersion(m.getVersionId());
        var p = pathForVersion(draft.getId());
        access.organizer(actor, p.getOrganizationId());
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
            .orElseThrow(() ->
                new NotFoundException(
                    "LEARNING_NOT_FOUND",
                    "Resource not found"
                )
            );
        var m = moduleEntity(r.getModuleId());
        var draft = draftVersion(m.getVersionId());
        access.organizer(
            actor,
            pathForVersion(draft.getId()).getOrganizationId()
        );
        media.deleteForResources(List.of(id));
        resources.deleteById(id);
    }

    @Transactional
    public Map<String, Object> cloneVersion(UUID a, UUID p) {
        var path = paths
            .findForUpdate(p)
            .orElseThrow(() ->
                new NotFoundException("LEARNING_NOT_FOUND", "Path not found")
            );
        access.organizer(a, path.getOrganizationId());
        var source = versions
            .findFirstByPathIdAndStatusOrderByVersionNoDesc(p, "PUBLISHED")
            .orElseThrow(() ->
                new NotFoundException(
                    "LEARNING_VERSION_NOT_FOUND",
                    "No published version to clone"
                )
            );
        var existing = versions.findFirstByPathIdAndStatusOrderByVersionNoDesc(
            p,
            "DRAFT"
        );
        if (existing.isPresent()) return versionResponse(existing.get());
        int next =
            versions
                .findByPathIdOrderByVersionNoDesc(p)
                .stream()
                .mapToInt(LearningPathVersionEntity::getVersionNo)
                .max()
                .orElse(0) + 1;
        var draft = versions.save(
            new LearningPathVersionEntity(
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
        var resourceMapping = new HashMap<UUID, UUID>();
        for (var sourceModule : modules.findByVersionIdOrderByPosition(
            source.getId()
        )) {
            var draftModule = modules.save(
                new LearningModuleEntity(
                    UUID.randomUUID(),
                    draft.getId(),
                    sourceModule.getPosition(),
                    sourceModule.getTitle()
                )
            );
            for (var sourceResource : resources.findByModuleIdOrderByPosition(
                sourceModule.getId()
            )) {
                var cloned = resources.save(
                    new LearningResourceEntity(
                        UUID.randomUUID(),
                        draftModule.getId(),
                        sourceResource.getPosition(),
                        sourceResource.getKind(),
                        sourceResource.getTitle(),
                        sourceResource.getBody(),
                        sourceResource.getUrl()
                    )
                );
                resourceMapping.put(sourceResource.getId(), cloned.getId());
            }
        }
        assessments.clonePublishedAssessments(source.getId(), draft.getId());
        media.cloneResourceAssets(resourceMapping);
        return versionResponse(draft);
    }

    private Map<String, Object> versionResponse(LearningPathVersionEntity v) {
        return Map.of(
            "id",
            v.getId(),
            "path_id",
            v.getPathId(),
            "version_no",
            v.getVersionNo(),
            "status",
            v.getStatus()
        );
    }

    @Transactional
    public Map<String, Object> policy(UUID a, UUID i, boolean r, boolean q) {
        var v = draftVersion(i);
        access.organizer(a, path(v.getPathId()).getOrganizationId());
        if (!r && !q) throw new BadRequestException(
            "COMPLETION_POLICY_EMPTY",
            "Select at least one completion requirement"
        );
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
            .orElseThrow(() ->
                new NotFoundException(
                    "LEARNING_VERSION_NOT_FOUND",
                    "Learning version not found"
                )
            );
        paths.findForUpdate(context.getPathId()).orElseThrow();
        var v = draftVersion(i);
        access.organizer(a, path(v.getPathId()).getOrganizationId());
        var resourceRows = modules
            .findByVersionIdOrderByPosition(i)
            .stream()
            .flatMap(module ->
                resources.findByModuleIdOrderByPosition(module.getId()).stream()
            )
            .toList();
        if (resourceRows.isEmpty()) throw new BadRequestException(
            "LEARNING_EMPTY",
            "Add at least one learning resource before publishing"
        );
        var mediaResources = resourceRows
            .stream()
            .filter(
                resource ->
                    "FILE".equals(resource.getKind()) ||
                    "AUDIO".equals(resource.getKind())
            )
            .map(LearningResourceEntity::getId)
            .toList();
        if (!media.hasAssetsForAll(mediaResources)) {
            throw new BadRequestException(
                "LEARNING_MEDIA_NOT_READY",
                "Every FILE and AUDIO resource must have an attached media asset before publishing"
            );
        }
        if (v.getStatus().equals("PUBLISHED")) {
            throw new com.skillproof.backend.common.exception.ConflictException(
                "LEARNING_VERSION_IMMUTABLE",
                "Published learning versions cannot be edited"
            );
        }
        if (assessments.hasDraftAssessments(i)) throw new BadRequestException(
            "LEARNING_ASSESSMENT_DRAFT",
            "Publish or remove all draft assessments before publishing the path version"
        );
        if (
            policies.findById(i).orElseThrow().isRequireOfficialAssessments() &&
            !assessments.hasPublishedOfficialAssessment(i)
        ) {
            throw new BadRequestException(
                "LEARNING_ASSESSMENT_NOT_READY",
                "An official assessment is required before publishing"
            );
        }
        versions
            .findFirstByPathIdAndStatusOrderByVersionNoDesc(
                v.getPathId(),
                "PUBLISHED"
            )
            .ifPresent(previous -> previous.archive());
        versions.flush();
        v.publish();
        versions.saveAndFlush(v);
        return Map.of("id", i, "status", "PUBLISHED");
    }

    public Map<String, Object> outline(UUID a, UUID i) {
        var v = versions
            .findById(i)
            .orElseThrow(() ->
                new NotFoundException(
                    "LEARNING_VERSION_NOT_FOUND",
                    "Learning version not found"
                )
            );
        access.organizer(a, path(v.getPathId()).getOrganizationId());
        var policy = policies
            .findById(i)
            .orElseThrow(() ->
                new NotFoundException(
                    "COMPLETION_POLICY_NOT_FOUND",
                    "Completion policy not found"
                )
            );
        var moduleRows = modules
            .findByVersionIdOrderByPosition(i)
            .stream()
            .map(module ->
                Map.<String, Object>of(
                    "id",
                    module.getId(),
                    "version_id",
                    module.getVersionId(),
                    "position",
                    module.getPosition(),
                    "title",
                    module.getTitle(),
                    "resources",
                    resources
                        .findByModuleIdOrderByPosition(module.getId())
                        .stream()
                        .map(resource -> {
                            var row = new java.util.LinkedHashMap<
                                String,
                                Object
                            >();
                            row.put("id", resource.getId());
                            row.put("module_id", resource.getModuleId());
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
                "path_id",
                v.getPathId(),
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
        access.organizer(a, path.getOrganizationId());
        return versions
            .findByPathIdOrderByVersionNoDesc(p)
            .stream()
            .map(v ->
                Map.<String, Object>of(
                    "id",
                    v.getId(),
                    "path_id",
                    v.getPathId(),
                    "version_no",
                    v.getVersionNo(),
                    "status",
                    v.getStatus()
                )
            )
            .toList();
    }
}
