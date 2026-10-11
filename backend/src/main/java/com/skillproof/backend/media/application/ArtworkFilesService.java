package com.skillproof.backend.media.application;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.media.application.port.MediaObjectStore;
import com.skillproof.backend.media.contract.ArtworkFiles;
import com.skillproof.backend.media.infrastructure.persistence.MediaAssetRepository;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ArtworkFilesService implements ArtworkFiles {
    private final MediaAssetRepository files;
    private final MediaObjectStore objects;

    public ArtworkFilesService(MediaAssetRepository files, MediaObjectStore objects) {
        this.files = files;
        this.objects = objects;
    }

    public void requireImage(UUID id, String scope, UUID owner) {
        var f =
                files.findById(id)
                        .orElseThrow(
                                () -> new NotFoundException("IMAGE_NOT_FOUND", "Image not found"));
        UUID target = "BRAND".equals(scope) ? f.getOrganizationId() : f.getCourseId();
        if (!scope.equals(f.getScope())
                || !owner.equals(target)
                || !f.getMimeType().startsWith("image/")
                || f.getSizeBytes() > 10485760)
            throw new BadRequestException("IMAGE_INVALID", "Image scope mismatch");
    }

    public Image image(UUID id) {
        var f =
                files.findById(id)
                        .orElseThrow(
                                () -> new NotFoundException("IMAGE_NOT_FOUND", "Image not found"));
        if (!java.util.Set.of("BRAND", "COURSE_COVER").contains(f.getScope()))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Public artwork only");
        return new Image(f.getMimeType(), f.getSizeBytes(), objects.open(f.getStorageKey()));
    }
}
