package com.skillproof.backend.media;
import com.skillproof.backend.media.infrastructure.cleanup.MediaCleanupReservation;
import com.skillproof.backend.media.infrastructure.cleanup.MediaCleanupWorker;
import com.skillproof.backend.media.infrastructure.cleanup.MediaCleanupRepository;
import com.skillproof.backend.media.infrastructure.cleanup.MediaCleanupTask;
import com.skillproof.backend.media.infrastructure.persistence.MediaAssetRepository;
import com.skillproof.backend.media.infrastructure.persistence.MediaAssetEntity;
import com.skillproof.backend.media.application.port.MediaObjectStore;
import com.skillproof.backend.media.application.MediaAccessPolicy;
import com.skillproof.backend.media.application.MediaService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.skillproof.backend.course.contract.CourseResourceAccessQuery;
import java.util.*;
import org.junit.jupiter.api.Test;

class MediaLifecycleTest {

    private final MediaAssetRepository assets = mock(
        MediaAssetRepository.class
    );
    private final MediaCleanupRepository cleanup = mock(
        MediaCleanupRepository.class
    );
    private final MediaAccessPolicy policy = mock(MediaAccessPolicy.class);
    private final MediaObjectStore store = mock(MediaObjectStore.class);
    private final MediaService service = new MediaService(
        assets,
        store,
        policy,
        mock(CourseResourceAccessQuery.class),
        cleanup, mock(MediaCleanupReservation.class),
        mock(com.skillproof.backend.assignment.contract.AssignmentMediaAccess.class),
        mock(com.skillproof.backend.library.contract.LibraryMediaAccess.class),
        mock(com.skillproof.backend.media.infrastructure.persistence.StorageObjectRepository.class)
    );

    private MediaAssetEntity asset(String scope) {
        var a = new MediaAssetEntity();
        a.setId(UUID.randomUUID());
        a.setStorageKey(UUID.randomUUID());
        a.setScope(scope);
        a.setOrganizationId("ORGANIZATION".equals(scope)
            ? UUID.randomUUID()
            : null);
        a.setResourceId(UUID.randomUUID());
        when(assets.findById(a.getId())).thenReturn(Optional.of(a));
        return a;
    }

    @Test
    void submittedEvidenceIsDetachedWithoutDeletingHistory() {
        var a = asset("ORGANIZATION");
        when(
            policy.retainOrganizationDocument(a.getOrganizationId(), a.getId())
        ).thenReturn(true);
        service.remove(UUID.randomUUID(), a.getId());
        assertFalse(a.getApplicationAttachmentActive());
        verify(assets).save(a);
        verify(assets, never()).delete(any());
        verifyNoInteractions(cleanup, store);
    }

    @Test
    void referenceRemovalQueuesCheckWithoutDeletingSharedBytes() {
        var a = asset("RESOURCE");
        when(assets.countByStorageKey(a.getStorageKey())).thenReturn(1L);
        service.remove(UUID.randomUUID(), a.getId());
        verify(assets).delete(a);
        verify(cleanup).save(any(MediaCleanupTask.class));
        verifyNoInteractions(store);
    }

    @Test
    void cleanupPreservesReferencedStorageBytes() {
        var task = new MediaCleanupTask(UUID.randomUUID());
        var storage = mock(com.skillproof.backend.media.infrastructure.persistence.StorageObjectRepository.class);
        when(cleanup.findById(task.getId())).thenReturn(Optional.of(task));
        when(cleanup.lock(task.getId())).thenReturn(Optional.of(task));
        when(storage.lock(task.getStorageKey())).thenReturn(Optional.of(new com.skillproof.backend.media.infrastructure.persistence.StorageObjectEntity(task.getStorageKey(),false,java.time.Instant.now())));
        when(assets.countByStorageKey(task.getStorageKey())).thenReturn(1L);
        new com.skillproof.backend.media.infrastructure.cleanup.MediaCleanupProcessor(cleanup,storage,assets,store).process(task.getId());
        verifyNoInteractions(store);
        verify(cleanup).delete(task);
    }

    @Test
    void storageFailureKeepsTaskAndCanBeDeferred() {
        var task = new MediaCleanupTask(UUID.randomUUID());
        var storage = mock(com.skillproof.backend.media.infrastructure.persistence.StorageObjectRepository.class);
        when(cleanup.findById(task.getId())).thenReturn(Optional.of(task));
        when(cleanup.lock(task.getId())).thenReturn(Optional.of(task));
        var object = new com.skillproof.backend.media.infrastructure.persistence.StorageObjectEntity(task.getStorageKey(),false,java.time.Instant.now());
        when(storage.lock(task.getStorageKey())).thenReturn(Optional.of(object));
        doThrow(new IllegalStateException("Storage unavailable")).when(store).delete(task.getStorageKey());
        var processor = new com.skillproof.backend.media.infrastructure.cleanup.MediaCleanupProcessor(cleanup,storage,assets,store);
        assertThrows(IllegalStateException.class,()->processor.process(task.getId()));
        assertFalse(object.getDeleted());
        verify(cleanup,never()).delete(any());
        processor.retryLater(task.getId());
        assertEquals(1,task.getAttempts());
        verify(cleanup).save(task);
    }
}
