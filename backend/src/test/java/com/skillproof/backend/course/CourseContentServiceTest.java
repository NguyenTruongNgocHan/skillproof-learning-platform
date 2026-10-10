package com.skillproof.backend.course;

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
import com.skillproof.backend.course.application.CourseAccess;
import com.skillproof.backend.course.application.CourseContentService;
import com.skillproof.backend.course.contract.CourseAssessmentDependency;
import com.skillproof.backend.course.contract.CourseMediaDependency;
import com.skillproof.backend.course.infrastructure.CompletionPolicyJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseContextRepository;
import com.skillproof.backend.course.infrastructure.CourseModuleEntity;
import com.skillproof.backend.course.infrastructure.CourseModuleJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseEntity;
import com.skillproof.backend.course.infrastructure.CourseJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseVersionEntity;
import com.skillproof.backend.course.infrastructure.CourseResourceJpaRepository;

class CourseContentServiceTest {

    private final CourseJpaRepository paths = mock(
        CourseJpaRepository.class
    );
    private final CourseModuleJpaRepository modules = mock(
        CourseModuleJpaRepository.class
    );
    private final CourseResourceJpaRepository resources = mock(
        CourseResourceJpaRepository.class
    );
    private final CourseContextRepository versions = mock(
        CourseContextRepository.class
    );
    private final CompletionPolicyJpaRepository policies = mock(
        CompletionPolicyJpaRepository.class
    );
    private final CourseAccess access = mock(CourseAccess.class);
    private final CourseMediaDependency media = mock(
        CourseMediaDependency.class
    );
    private final CourseAssessmentDependency assessments = mock(
        CourseAssessmentDependency.class
    );
    private final CourseContentService service = new CourseContentService(
        modules,
        resources,
        versions,
        access,
        media,
        assessments, mock(com.skillproof.backend.course.infrastructure.persistence.LessonRepository.class), mock(com.skillproof.backend.assignment.contract.AssignmentVersionBridge.class)
    );

    @org.junit.jupiter.api.BeforeEach
    void initializePersistenceContext() {
        org.springframework.test.util.ReflectionTestUtils.setField(service,"entityManager",mock(jakarta.persistence.EntityManager.class));
    }

    @Test
    void resolvesModuleAuthorizationThroughVersionAndPathIds() {
        UUID actor = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();
        var version = new CourseVersionEntity(
            versionId,
            courseId,
            2,
            "DRAFT",
            Instant.now()
        );
        var path = new CourseEntity(
            courseId,
            UUID.randomUUID(),
            "path",
            "Path",
            "Summary",
            actor,
            Instant.now()
        );
        var module = new CourseModuleEntity(moduleId, versionId, 1, "Module");

        when(versions.findForUpdate(versionId)).thenReturn(
            Optional.of(version)
        );
        when(versions.findCourseByVersionId(versionId)).thenReturn(
            Optional.of(path)
        );
        when(
            modules.save(
                org.mockito.ArgumentMatchers.any(CourseModuleEntity.class)
            )
        ).thenReturn(module);

        org.springframework.test.util.ReflectionTestUtils.setField(service, "entityManager", mock(jakarta.persistence.EntityManager.class));
        var result = service.module(actor, versionId, 1, "Module");

        assertEquals(moduleId, result.get("id"));
        verify(versions).findCourseByVersionId(versionId);
    }

    @Test
    void rejectsMutationOfPublishedVersionBeforeOrganizationLookup() {
        UUID versionId = UUID.randomUUID();
        when(versions.findForUpdate(versionId)).thenReturn(
            Optional.of(
                new CourseVersionEntity(
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
