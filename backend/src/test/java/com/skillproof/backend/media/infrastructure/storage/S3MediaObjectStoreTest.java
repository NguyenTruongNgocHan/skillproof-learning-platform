package com.skillproof.backend.media.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.skillproof.backend.media.application.exception.StorageException;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.exception.SdkClientException;

class S3MediaObjectStoreTest {

    @Test
    void acceptsHealthyBucketAndClosesOwnedClient() {
        S3Client client = mock(S3Client.class);
        when(client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());

        var store = new S3MediaObjectStore("skillproof-media", "us-east-1",
                "http://localhost:9000", "access", "secret", "skillproof/", client, true);

        store.close();
        verify(client).headBucket(any(HeadBucketRequest.class));
        verify(client).close();
    }

    @Test
    void missingBucketHasContextAndPreservesCause() {
        S3Client client = mock(S3Client.class);
        var cause = S3Exception.builder().statusCode(404).message("not found").build();
        when(client.headBucket(any(HeadBucketRequest.class))).thenThrow(cause);

        StorageException error = assertThrows(StorageException.class,
                () -> new S3MediaObjectStore("skillproof-media", "us-east-1",
                        "http://localhost:9000", "access", "secret", "skillproof/", client, false));

        assertEquals("STORAGE_BUCKET_UNAVAILABLE", error.code());
        assertEquals(cause, error.getCause());
        verify(client, never()).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    void accessDeniedDoesNotAttemptBucketCreation() {
        S3Client client = mock(S3Client.class);
        var cause = S3Exception.builder().statusCode(403).message("denied").build();
        when(client.headBucket(any(HeadBucketRequest.class))).thenThrow(cause);

        StorageException error = assertThrows(StorageException.class,
                () -> new S3MediaObjectStore("skillproof-media", "us-east-1",
                        "http://localhost:9000", "access", "secret", "skillproof/", client, false));

        assertEquals("STORAGE_ACCESS_DENIED", error.code());
        verify(client, never()).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    void connectionFailureHasStableCodeAndCause() {
        S3Client client = mock(S3Client.class);
        var cause = SdkClientException.create("timeout");
        when(client.headBucket(any(HeadBucketRequest.class))).thenThrow(cause);

        StorageException error = assertThrows(StorageException.class,
                () -> new S3MediaObjectStore("skillproof-media", "us-east-1",
                        "http://localhost:9000", "access", "secret", "skillproof/", client, false));

        assertEquals("STORAGE_CONNECTION_FAILED", error.code());
        assertEquals(cause, error.getCause());
    }

    @Test
    void rejectsInvalidEndpointBeforeRuntimeUse() {
        assertThrows(IllegalArgumentException.class,
                () -> new S3MediaObjectStore("skillproof-media", "us-east-1",
                        "ftp://object-storage", "access", "secret", "skillproof/"));
    }

    @Test
    void injectedClientIsNotClosedByAdapter() {
        S3Client client = mock(S3Client.class);
        when(client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());
        var store = new S3MediaObjectStore("skillproof-media", "us-east-1",
                "", "", "", "skillproof/", client, false);

        store.close();

        verify(client, never()).close();
    }
}
