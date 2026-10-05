package com.skillproof.backend.media;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.skillproof.backend.learning.contract.LearningResourceAccessQuery;
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
        mock(LearningResourceAccessQuery.class),
        cleanup
    );

    private MediaAsset asset(String scope) {
        var a = new MediaAsset();
        a.id = UUID.randomUUID();
        a.storageKey = UUID.randomUUID();
        a.scope = scope;
        a.organizationId = "ORGANIZATION".equals(scope)
            ? UUID.randomUUID()
            : null;
        a.resourceId = UUID.randomUUID();
        when(assets.findById(a.id)).thenReturn(Optional.of(a));
        return a;
    }

    @Test
    void submittedEvidenceIsDetachedWithoutDeletingHistory() {
        var a = asset("ORGANIZATION");
        when(
            policy.retainOrganizationDocument(a.organizationId, a.id)
        ).thenReturn(true);
        service.remove(UUID.randomUUID(), a.id);
        assertFalse(a.applicationAttachmentActive);
        verify(assets).save(a);
        verify(assets, never()).delete(any());
        verifyNoInteractions(cleanup, store);
    }

    @Test
    void referenceRemovalQueuesCheckWithoutDeletingSharedBytes() {
        var a = asset("RESOURCE");
        when(assets.countByStorageKey(a.storageKey)).thenReturn(1L);
        service.remove(UUID.randomUUID(), a.id);
        verify(assets).delete(a);
        verify(cleanup).save(any(MediaCleanupTask.class));
        verifyNoInteractions(store);
    }

    @Test
    void cleanupWorkerPreservesReferencedStorageBytes() {
        var task = new MediaCleanupTask(UUID.randomUUID());
        when(
            cleanup.findByNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                any(),
                any()
            )
        ).thenReturn(List.of(task));
        when(assets.countByStorageKey(task.storageKey)).thenReturn(1L);
        new MediaCleanupWorker(cleanup, assets, store).retry();
        verifyNoInteractions(store);
        verify(cleanup).deleteById(task.id);
    }

    @Test
    void storageFailureLeavesADeferredCleanupTask() {
        var task = new MediaCleanupTask(UUID.randomUUID());
        when(
            cleanup.findByNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
                any(),
                any()
            )
        ).thenReturn(List.of(task));
        doThrow(new IllegalStateException("Storage unavailable"))
            .when(store)
            .delete(task.storageKey);
        new MediaCleanupWorker(cleanup, assets, store).retry();
        assertEquals(1, task.attempts);
        assertTrue(task.nextAttemptAt.isAfter(task.createdAt));
        verify(cleanup).save(task);
        verify(cleanup, never()).deleteById(any());
    }
}
