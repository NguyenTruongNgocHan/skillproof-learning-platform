package com.skillproof.backend.media.infrastructure.storage;

import com.skillproof.backend.media.application.port.DirectObjectUpload;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("test")
public class TestDirectObjectUpload
        implements com.skillproof.backend.media.application.port.MultipartObjectUpload,
                DirectObjectUpload,
                com.skillproof.backend.media.application.port.MediaPlaybackLinks {
    public String signedPut(UUID id) {
        throw new UnsupportedOperationException(
                "Direct object upload requires configured object storage");
    }

    public long size(UUID id) {
        throw new UnsupportedOperationException(
                "Direct object upload requires configured object storage");
    }

    public String signedGet(java.util.UUID key) {
        throw new UnsupportedOperationException(
                "Signed object access requires configured S3 storage");
    }

    public String start(java.util.UUID key) {
        throw new UnsupportedOperationException("Multipart upload requires configured S3");
    }

    public String signedPart(java.util.UUID key, String id, int number) {
        throw new UnsupportedOperationException("Multipart upload requires configured S3");
    }

    public java.util.List<com.skillproof.backend.media.application.port.MultipartObjectUpload.Part>
            parts(java.util.UUID key, String id) {
        throw new UnsupportedOperationException("Multipart upload requires configured S3");
    }

    public void assemble(
            java.util.UUID key,
            String id,
            java.util.List<com.skillproof.backend.media.application.port.MultipartObjectUpload.Part>
                    parts) {
        throw new UnsupportedOperationException("Multipart upload requires configured S3");
    }

    public void abort(java.util.UUID key, String id) {}
}
