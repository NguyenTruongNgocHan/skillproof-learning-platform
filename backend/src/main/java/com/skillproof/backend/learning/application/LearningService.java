package com.skillproof.backend.learning.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.learning.contract.LearningAssessmentDependency;
import com.skillproof.backend.learning.contract.LearningMediaDependency;
import com.skillproof.backend.learning.infrastructure.CompletionPolicyJpaRepository;
import com.skillproof.backend.learning.infrastructure.LearningContextRepository;
import com.skillproof.backend.learning.infrastructure.LearningModuleEntity;
import com.skillproof.backend.learning.infrastructure.LearningModuleJpaRepository;
import com.skillproof.backend.learning.infrastructure.LearningPathEntity;
import com.skillproof.backend.learning.infrastructure.LearningPathJpaRepository;
import com.skillproof.backend.learning.infrastructure.LearningResourceEntity;
import com.skillproof.backend.learning.infrastructure.LearningResourceJpaRepository;

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

    public LearningService(LearningPathJpaRepository p, LearningModuleJpaRepository m, LearningResourceJpaRepository r, LearningContextRepository v, CompletionPolicyJpaRepository cp, LearningAccess a, LearningMediaDependency md, LearningAssessmentDependency ad) {
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
        return paths.findById(id).orElseThrow(() -> new NotFoundException("LEARNING_NOT_FOUND", "Learning record not found"));
    }

    public List<Map<String, Object>> owned(UUID actor, UUID org) {
        access.organizer(actor, org);
        return paths.findByOrganizationIdOrderByCreatedAtDesc(org).stream().map(p -> Map.<String, Object>of("id", p.getId(), "organization_id", p.getOrganizationId(), "slug", p.getSlug(), "title", p.getTitle(), "summary", p.getSummary())).toList();
    }

    @Transactional
    public Map<String, Object> create(UUID actor, UUID org, String slug, String title, String summary) {
        access.organizer(actor, org);
        var p = paths.save(new LearningPathEntity(UUID.randomUUID(), org, slug.toLowerCase(), title.trim(), summary.trim(), actor, Instant.now()));
        return Map.of("id", p.getId(), "organization_id", org, "slug", p.getSlug(), "title", p.getTitle(), "summary", p.getSummary());
    }

    @Transactional
    public Map<String, Object> updatePath(UUID actor, UUID id, String title, String summary) {
        var p = path(id);
        access.organizer(actor, p.getOrganizationId());
        p.edit(title.trim(), summary.trim());
        return Map.of("id", p.getId(), "title", p.getTitle(), "summary", p.getSummary());
    }

    private LearningModuleEntity moduleEntity(UUID id) {
        return modules.findById(id).orElseThrow(() -> new NotFoundException("LEARNING_NOT_FOUND", "Learning module not found"));
    }

    @Transactional
    public Map<String, Object> module(UUID actor, UUID version, int position, String title) {
        var p = path(version);
        access.organizer(actor, p.getOrganizationId());
        var m = modules.save(new LearningModuleEntity(UUID.randomUUID(), version, position, title.trim()));
        return Map.of("id", m.getId(), "version_id", version, "position", position, "title", title.trim());
    }

    @Transactional
    public Map<String, Object> updateModule(UUID actor, UUID id, int position, String title) {
        var m = moduleEntity(id);
        var p = path(m.getVersionId());
        access.organizer(actor, p.getOrganizationId());
        m.edit(position, title.trim());
        return Map.of("id", id, "position", position, "title", title.trim());
    }

    @Transactional
    public void deleteModule(UUID actor, UUID id) {
        var m = moduleEntity(id);
        var p = path(m.getVersionId());
        access.organizer(actor, p.getOrganizationId());
        var ids = resources.findByModuleIdOrderByPosition(id).stream().map(LearningResourceEntity::getId).toList();
        media.deleteForResources(ids);
        resources.deleteAllById(ids);
        modules.deleteById(id);
    }

    private boolean valid(String k, String b, String u) {
        return "ARTICLE".equals(k) ? b != null && !b.isBlank() && u == null : ("LINK".equals(k) || "VIDEO".equals(k)) && b == null && u != null && u.matches("https://.+");
    }

    @Transactional
    public Map<String, Object> resource(UUID actor, UUID module, int position, String kind, String title, String body, String url) {
        var m = moduleEntity(module);
        var p = path(m.getVersionId());
        access.organizer(actor, p.getOrganizationId());
        if (!valid(kind, body, url)) {
            throw new BadRequestException("RESOURCE_INVALID", "Invalid resource");
        
        }var r = resources.save(new LearningResourceEntity(UUID.randomUUID(), module, position, kind, title.trim(), body, url));
        return Map.of("id", r.getId(), "module_id", module, "position", position, "kind", kind, "title", title.trim());
    }

    @Transactional
    public Map<String, Object> updateResource(UUID actor, UUID id, int position, String kind, String title, String body, String url) {
        var r = resources.findById(id).orElseThrow(() -> new NotFoundException("LEARNING_NOT_FOUND", "Resource not found"));
        var m = moduleEntity(r.getModuleId());
        var p = path(m.getVersionId());
        access.organizer(actor, p.getOrganizationId());
        if (!valid(kind, body, url)) {
            throw new BadRequestException("RESOURCE_INVALID", "Invalid resource");
        
        }r.edit(position, kind, title.trim(), body, url);
        return Map.of("id", id, "position", position, "kind", kind, "title", title.trim());
    }

    @Transactional
    public void deleteResource(UUID actor, UUID id) {
        var r = resources.findById(id).orElseThrow(() -> new NotFoundException("LEARNING_NOT_FOUND", "Resource not found"));
        var m = moduleEntity(r.getModuleId());
        access.organizer(actor, path(m.getVersionId()).getOrganizationId());
        media.deleteForResources(List.of(id));
        resources.deleteById(id);
    }

    public Map<String, Object> cloneVersion(UUID a, UUID p) {
        access.organizer(a, path(p).getOrganizationId());
        return Map.of("pathId", p, "status", "DRAFT");
    }

    public Map<String, Object> policy(UUID a, UUID i, boolean r, boolean q) {
        var v = versions.findById(i).orElseThrow();
        access.organizer(a, path(v.getPathId()).getOrganizationId());
        var p = policies.findById(i).orElseThrow();
        p.configure(r, q);
        policies.save(p);
        return Map.of("versionId", i, "requireAllResources", r, "requireOfficialAssessments", q);
    }

    public Map<String, Object> publish(UUID a, UUID i) {
        var v = versions.findById(i).orElseThrow();
        access.organizer(a, path(v.getPathId()).getOrganizationId());
        v.publish();
        versions.save(v);
        return Map.of("id", i, "status", "PUBLISHED");
    }

    public Map<String, Object> outline(UUID a, UUID i) {
        return Map.of("versionId", i, "modules", modules.findByVersionIdOrderByPosition(i));
    }

    public List<Map<String, Object>> versions(UUID a, UUID p) {
        var path = path(p);
        access.organizer(a, path.getOrganizationId());
        return versions.findByPathIdOrderByVersionNoDesc(p).stream().map(v -> Map.<String, Object>of("id", v.getId(), "path_id", v.getPathId(), "version_no", v.getVersionNo(), "status", v.getStatus())).toList();
    }
}
