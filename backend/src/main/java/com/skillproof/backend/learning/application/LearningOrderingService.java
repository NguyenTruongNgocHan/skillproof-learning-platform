package com.skillproof.backend.learning.application;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.learning.infrastructure.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Owns atomic ordering of draft content, including collision-free position updates. */
@Service
public class LearningOrderingService {

    private final LearningContextRepository versions;
    private final LearningModuleJpaRepository modules;
    private final LearningResourceJpaRepository resources;
    private final LearningAccess access;

    public LearningOrderingService(
        LearningContextRepository versions,
        LearningModuleJpaRepository modules,
        LearningResourceJpaRepository resources,
        LearningAccess access
    ) {
        this.versions = versions;
        this.modules = modules;
        this.resources = resources;
        this.access = access;
    }

    private void authorize(UUID actor, UUID versionId) {
        var version = versions
            .findForUpdate(versionId)
            .orElseThrow(() ->
                new NotFoundException(
                    "LEARNING_VERSION_NOT_FOUND",
                    "Version not found"
                )
            );
        var path = versions.findPathByVersionId(versionId).orElseThrow();
        access.organizer(actor, path.getOrganizationId());
        if (!"DRAFT".equals(version.getStatus())) throw new ConflictException(
            "LEARNING_VERSION_IMMUTABLE",
            "Reorder only draft content"
        );
    }

    private void validate(List<UUID> supplied, List<UUID> existing) {
        if (
            supplied == null ||
            supplied.size() != existing.size() ||
            new HashSet<>(supplied).size() != supplied.size() ||
            !new HashSet<>(supplied).equals(new HashSet<>(existing))
        ) throw new BadRequestException(
            "INVALID_CONTENT_ORDER",
            "Supply each current item exactly once"
        );
    }

    @Transactional
    public void modules(UUID actor, UUID versionId, List<UUID> ids) {
        authorize(actor, versionId);
        var rows = modules.findByVersionIdOrderByPosition(versionId);
        validate(ids, rows.stream().map(LearningModuleEntity::getId).toList());
        int temporary = Math.addExact(
            rows
                .stream()
                .mapToInt(LearningModuleEntity::getPosition)
                .max()
                .orElse(0),
            1
        );
        var indexed = new HashMap<UUID, LearningModuleEntity>();
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
    public void resources(UUID actor, UUID moduleId, List<UUID> ids) {
        var module = modules
            .findById(moduleId)
            .orElseThrow(() ->
                new NotFoundException(
                    "LEARNING_MODULE_NOT_FOUND",
                    "Module not found"
                )
            );
        authorize(actor, module.getVersionId());
        var rows = resources.findByModuleIdOrderByPosition(moduleId);
        validate(
            ids,
            rows.stream().map(LearningResourceEntity::getId).toList()
        );
        int temporary = Math.addExact(
            rows
                .stream()
                .mapToInt(LearningResourceEntity::getPosition)
                .max()
                .orElse(0),
            1
        );
        var indexed = new HashMap<UUID, LearningResourceEntity>();
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
}
