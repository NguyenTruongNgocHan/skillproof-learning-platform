package com.skillproof.backend.learning;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.learning.application.LearningAccess;
import com.skillproof.backend.learning.application.LearningService;
import com.skillproof.backend.learning.contract.LearningAssessmentDependency;
import com.skillproof.backend.learning.contract.LearningMediaDependency;
import com.skillproof.backend.learning.infrastructure.CompletionPolicyJpaRepository;
import com.skillproof.backend.learning.infrastructure.LearningContextRepository;
import com.skillproof.backend.learning.infrastructure.LearningModuleEntity;
import com.skillproof.backend.learning.infrastructure.LearningModuleJpaRepository;
import com.skillproof.backend.learning.infrastructure.LearningPathEntity;
import com.skillproof.backend.learning.infrastructure.LearningPathJpaRepository;
import com.skillproof.backend.learning.infrastructure.LearningPathVersionEntity;
import com.skillproof.backend.learning.infrastructure.LearningResourceJpaRepository;

class LearningServiceTest {

    private final LearningPathJpaRepository paths = mock(
        LearningPathJpaRepository.class
    );
    private final LearningModuleJpaRepository modules = mock(
        LearningModuleJpaRepository.class
    );
    private final LearningResourceJpaRepository resources = mock(
        LearningResourceJpaRepository.class
    );
    private final LearningContextRepository versions = mock(
        LearningContextRepository.class
    );
    private final CompletionPolicyJpaRepository policies = mock(
        CompletionPolicyJpaRepository.class
    );
    private final LearningAccess access = mock(LearningAccess.class);
    private final LearningMediaDependency media = mock(
        LearningMediaDependency.class
    );
    private final LearningAssessmentDependency assessments = mock(
        LearningAssessmentDependency.class
    );
    private final LearningService service = new LearningService(
        paths,
        modules,
        resources,
        versions,
        policies,
        access,
        media,
        assessments
    );

    @Test
    void resolvesModuleAuthorizationThroughVersionAndPathIds() {
        UUID actor = UUID.randomUUID();
        UUID pathId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();
        var version = new LearningPathVersionEntity(
            versionId,
            pathId,
            2,
            "DRAFT",
            Instant.now()
        );
        var path = new LearningPathEntity(
            pathId,
            UUID.randomUUID(),
            "path",
            "Path",
            "Summary",
            actor,
            Instant.now()
        );
        var module = new LearningModuleEntity(moduleId, versionId, 1, "Module");

        when(versions.findForUpdate(versionId)).thenReturn(
            Optional.of(version)
        );
        when(versions.findPathByVersionId(versionId)).thenReturn(
            Optional.of(path)
        );
        when(
            modules.save(
                org.mockito.ArgumentMatchers.any(LearningModuleEntity.class)
            )
        ).thenReturn(module);

        var result = service.module(actor, versionId, 1, "Module");

        assertEquals(moduleId, result.get("id"));
        verify(versions).findPathByVersionId(versionId);
    }

    @Test
    void rejectsMutationOfPublishedVersionBeforeOrganizationLookup() {
        UUID versionId = UUID.randomUUID();
        when(versions.findForUpdate(versionId)).thenReturn(
            Optional.of(
                new LearningPathVersionEntity(
                    versionId,
                    UUID.randomUUID(),
                    1,
                    "PUBLISHED",
                    Instant.now()
                )
            )
        );

        assertThrows(ConflictException.class, () ->
            service.module(UUID.randomUUID(), versionId, 1, "Module")
        );
    }
}
