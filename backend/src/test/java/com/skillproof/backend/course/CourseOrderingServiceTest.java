package com.skillproof.backend.course;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.course.application.*;
import com.skillproof.backend.course.infrastructure.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;

class CourseOrderingServiceTest {

    private final CourseContextRepository versions = mock(
        CourseContextRepository.class
    );
    private final CourseModuleJpaRepository modules = mock(
        CourseModuleJpaRepository.class
    );
    private final CourseResourceJpaRepository resources = mock(
        CourseResourceJpaRepository.class
    );
    private final CourseOrderingService service = new CourseOrderingService(
        versions,
        modules,
        resources,
        mock(CourseAccess.class), mock(com.skillproof.backend.course.infrastructure.persistence.LessonRepository.class)
    );
    private final UUID version = UUID.randomUUID(),
        actor = UUID.randomUUID();

    private List<CourseModuleEntity> setup(String status) {
        UUID path = UUID.randomUUID();
        when(versions.findForUpdate(version)).thenReturn(
            Optional.of(
                new CourseVersionEntity(
                    version,
                    path,
                    1,
                    status,
                    Instant.now()
                )
            )
        );
        when(versions.findCourseByVersionId(version)).thenReturn(
            Optional.of(
                new CourseEntity(
                    path,
                    UUID.randomUUID(),
                    "test",
                    "Test",
                    "Summary",
                    actor,
                    Instant.now()
                )
            )
        );
        var rows = List.of(
            new CourseModuleEntity(UUID.randomUUID(), version, 1, "First"),
            new CourseModuleEntity(UUID.randomUUID(), version, 2, "Second")
        );
        when(modules.findByVersionIdOrderByPosition(version)).thenReturn(rows);
        return rows;
    }

    @Test
    void reorderKeepsIdsAndProducesContiguousPositions() {
        var rows = setup("DRAFT");
        service.modules(
            actor,
            version,
            List.of(rows.get(1).getId(), rows.get(0).getId())
        );
        assertEquals(2, rows.get(0).getPosition());
        assertEquals(1, rows.get(1).getPosition());
        verify(modules, times(2)).flush();
    }

    @Test
    void foreignIdsCannotEnterTheOutline() {
        var rows = setup("DRAFT");
        assertThrows(BadRequestException.class, () ->
            service.modules(
                actor,
                version,
                List.of(rows.get(0).getId(), UUID.randomUUID())
            )
        );
        assertEquals(1, rows.get(0).getPosition());
        verify(modules, never()).flush();
    }

    @Test
    void publishedOrderCannotBeChanged() {
        var rows = setup("PUBLISHED");
        assertThrows(ConflictException.class, () ->
            service.modules(
                actor,
                version,
                List.of(rows.get(1).getId(), rows.get(0).getId())
            )
        );
        verify(modules, never()).flush();
    }
}
