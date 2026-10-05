package com.skillproof.backend.learning;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.learning.application.*;
import com.skillproof.backend.learning.infrastructure.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;

class LearningOrderingServiceTest {

    private final LearningContextRepository versions = mock(
        LearningContextRepository.class
    );
    private final LearningModuleJpaRepository modules = mock(
        LearningModuleJpaRepository.class
    );
    private final LearningResourceJpaRepository resources = mock(
        LearningResourceJpaRepository.class
    );
    private final LearningOrderingService service = new LearningOrderingService(
        versions,
        modules,
        resources,
        mock(LearningAccess.class)
    );
    private final UUID version = UUID.randomUUID(),
        actor = UUID.randomUUID();

    private List<LearningModuleEntity> setup(String status) {
        UUID path = UUID.randomUUID();
        when(versions.findForUpdate(version)).thenReturn(
            Optional.of(
                new LearningPathVersionEntity(
                    version,
                    path,
                    1,
                    status,
                    Instant.now()
                )
            )
        );
        when(versions.findPathByVersionId(version)).thenReturn(
            Optional.of(
                new LearningPathEntity(
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
            new LearningModuleEntity(UUID.randomUUID(), version, 1, "First"),
            new LearningModuleEntity(UUID.randomUUID(), version, 2, "Second")
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
