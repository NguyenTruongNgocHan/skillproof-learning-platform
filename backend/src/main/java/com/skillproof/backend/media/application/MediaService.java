package com.skillproof.backend.media.application;

import com.skillproof.backend.media.infrastructure.cleanup.MediaCleanupReservation;
import com.skillproof.backend.media.infrastructure.cleanup.MediaCleanupRepository;
import com.skillproof.backend.media.infrastructure.cleanup.MediaCleanupTask;
import com.skillproof.backend.media.infrastructure.persistence.MediaAssetRepository;
import com.skillproof.backend.media.infrastructure.persistence.MediaAssetEntity;
import com.skillproof.backend.media.application.exception.StorageException;
import com.skillproof.backend.media.domain.MediaSignature;
import com.skillproof.backend.media.application.port.MediaObjectStore;

import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CourseMediaDependency;
import com.skillproof.backend.course.contract.CourseResourceAccessQuery;
import com.skillproof.backend.media.contract.MediaAvatarQuery;
import com.skillproof.backend.organization.contract.OrganizationDocumentQuery;

import org.springframework.transaction.annotation.Transactional;

@Service
public class MediaService
        implements
        CourseMediaDependency,
        MediaAvatarQuery,
        OrganizationDocumentQuery, com.skillproof.backend.media.contract.SubmissionMediaQuery, com.skillproof.backend.media.contract.LibraryMediaQuery {

    private final com.skillproof.backend.assignment.contract.AssignmentMediaAccess submissions;
    private final com.skillproof.backend.library.contract.LibraryMediaAccess library;
    private final com.skillproof.backend.media.infrastructure.persistence.StorageObjectRepository storage;
    private final MediaAssetRepository repo;
    private final MediaCleanupReservation reservations;
    private final MediaCleanupRepository cleanup;
    private final MediaObjectStore objects;
    private final MediaAccessPolicy policy;
    private final CourseResourceAccessQuery resources;
    private static final long LIMIT = 100L * 1024 * 1024;

    public MediaService(
            MediaAssetRepository r,
            MediaObjectStore o,
            MediaAccessPolicy p,
            CourseResourceAccessQuery q,
            MediaCleanupRepository cleanup, MediaCleanupReservation reservations,
            com.skillproof.backend.assignment.contract.AssignmentMediaAccess submissions, com.skillproof.backend.library.contract.LibraryMediaAccess library, com.skillproof.backend.media.infrastructure.persistence.StorageObjectRepository storage
    ) {
        this.storage = storage;
        repo = r;
        this.submissions = submissions;
        this.library = library;
        this.cleanup = cleanup;
        this.reservations = reservations;
        objects = o;
        policy = p;
        resources = q;
    }

    private MediaAssetEntity one(UUID id) {
        return repo
                .findById(id)
                .orElseThrow(()
                        -> new NotFoundException("MEDIA_NOT_FOUND", "File not found")
                );
    }

    private Map<String, Object> meta(MediaAssetEntity m) {
        return Map.of(
                "id",
                m.getId(),
                "name",
                m.getOriginalName(),
                "mimeType",
                m.getMimeType(),
                "size",
                m.getSizeBytes(),
                "createdAt",
                m.getCreatedAt()
        );
    }

    @Transactional(timeout = 60)
    public Map<String, Object> upload(
            UUID actor,
            String scope,
            UUID target,
            MultipartFile file
    ) {
        policy.active(actor);
        if (file == null || file.isEmpty() || file.getSize() > LIMIT) {
            throw new BadRequestException(
                    "MEDIA_SIZE",
                    "File must contain 1 to 100 MB"
            );
        }
        if (scope.equals("AVATAR") && target != null) {
            throw new BadRequestException(
                    "MEDIA_TARGET",
                    "Avatar has no target"
            );
        }
        if (scope.equals("ORGANIZATION")) {
            if (target == null) {
                throw new BadRequestException(
                        "MEDIA_TARGET",
                        "Organization required"
                );
            }
            policy.writeOrganization(actor, target);
        }
        if (scope.equals("RESOURCE")) {
            if (target == null) {
                throw new BadRequestException(
                        "MEDIA_TARGET",
                        "Resource required"
                );
            }
            policy.draftResource(actor, target);
        }
        if ("SUBMISSION".equals(scope) || "LIBRARY".equals(scope)) {
            if (target == null) {
                throw new BadRequestException("MEDIA_TARGET", "Target required");
            }
            if ("SUBMISSION".equals(scope)) {
                submissions.requireWrite(actor, target);
            } else {
                library.requireWrite(actor, target);
            }
        }
        if (!Set.of("AVATAR", "ORGANIZATION", "RESOURCE", "LIBRARY", "SUBMISSION").contains(scope)) {
            throw new BadRequestException("MEDIA_SCOPE", "Invalid media scope");
        }
        String name = Optional.ofNullable(file.getOriginalFilename())
                .orElse("file")
                .replaceAll("[\\p{Cntrl}/\\\\]", "_");
        if (name.isBlank() || name.length() > 180) {
            throw new BadRequestException("MEDIA_NAME", "Invalid filename");
        }
        UUID id = UUID.randomUUID(),
                key = UUID.randomUUID();
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] head = new byte[16];
            int h = 0;
            long count = 0;
            try (InputStream in = file.getInputStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) {
                    count += n;
                    if (count > LIMIT) {
                        throw new BadRequestException(
                                "MEDIA_SIZE",
                                "File exceeds 100 MB"
                        );
                    }
                    if (h < head.length) {
                        int x = Math.min(n, head.length - h);
                        System.arraycopy(buf, 0, head, h, x);
                        h += x;
                    }
                    sha.update(buf, 0, n);
                }
            }
            if (count == 0) {
                throw new BadRequestException("MEDIA_SIZE", "Empty file");
            }
            String mime = MediaSignature.detect(Arrays.copyOf(head, h));
            if (scope.equals("AVATAR") && !mime.startsWith("image/")) {
                throw new BadRequestException(
                        "MEDIA_TYPE",
                        "Avatar must be JPEG, PNG or WebP"
                );
            }
            if (scope.equals("ORGANIZATION")
                    && !mime.equals("application/pdf")
                    && !mime.startsWith("image/")) {
                throw new BadRequestException(
                        "MEDIA_TYPE",
                        "Organization document must be PDF or image"
                );
            }
            if (scope.equals("RESOURCE")
                    && (("AUDIO".equals(resources.resourceKind(target)) && !mime.startsWith("audio/"))
                    || ("IMAGE".equals(resources.resourceKind(target)) && !mime.startsWith("image/")))) {
                throw new BadRequestException(
                        "MEDIA_TYPE",
                        "Audio lessons require an audio file"
                );
            }
            // Persist cleanup independently before creating external bytes.
            // Worker waits for upload transaction to finish and retains referenced bytes.
            reservations.reserve(key);
            storage.lock(key).orElseThrow();
            try (InputStream in = file.getInputStream()) {
                objects.put(key, in, count, mime);
            }
            var m = new MediaAssetEntity();
            m.setId(id);
            m.setOwnerUserId(actor);
            m.setScope(scope);
            m.setOrganizationId(scope.equals("ORGANIZATION") ? target : null);
            m.setResourceId(scope.equals("RESOURCE") ? target : null);
            m.setLibraryResourceId(scope.equals("LIBRARY") ? target : null);
            m.setSubmissionId(scope.equals("SUBMISSION") ? target : null);
            m.setOriginalName(name);
            m.setStorageKey(key);
            m.setMimeType(mime);
            m.setSizeBytes(count);
            m.setSha256(HexFormat.of().formatHex(sha.digest()));
            m.setCreatedAt(Instant.now());
            try {
                return meta(repo.saveAndFlush(m));
            } catch (RuntimeException e) {
                try {
                    objects.delete(key);
                } catch (RuntimeException cleanupFailure) {
                    e.addSuppressed(cleanupFailure);
                }
                throw e;
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Could not store file", e);
        }
    }

    public void verifyAvatar(UUID actor, UUID id) {
        var m = one(id);
        if (!"AVATAR".equals(m.getScope()) || !actor.equals(m.getOwnerUserId())) {
            throw new AccessDeniedException(
                    "Avatar must belong to your account"
            );
        }
    }

    public Map<String, Object> metadata(UUID id) {
        return meta(one(id));
    }

    public List<Map<String, Object>> list(
            UUID actor,
            String scope,
            UUID target
    ) {
        List<MediaAssetEntity> ms;
        if (scope.equals("AVATAR")) {
            policy.active(actor);
            ms = repo.findByScopeAndOwnerUserIdOrderByCreatedAtDesc(
                    scope,
                    actor
            );
        } else {
            if (target == null) {
                throw new BadRequestException(
                        "MEDIA_TARGET",
                        "Target required"
                );
            }
            if (scope.equals("ORGANIZATION")) {
                policy.adminOrOrganizer(actor, target);
                ms
                        = repo.findByScopeAndOrganizationIdAndApplicationAttachmentActiveTrueOrderByCreatedAtDesc(
                                scope,
                                target
                        );
            } else if (scope.equals("RESOURCE")) {
                policy.readResource(actor, target);
                ms = repo.findByScopeAndResourceIdOrderByCreatedAtDesc(
                        scope,
                        target
                );
            } else if ("LIBRARY".equals(scope)) {
                library.requireRead(actor, target);
                ms = repo.findByScopeAndLibraryResourceIdOrderByCreatedAtDesc(scope, target);
            } else if ("SUBMISSION".equals(scope)) {
                submissions.requireRead(actor, target);
                ms = repo.findByScopeAndSubmissionIdOrderByCreatedAtDesc(scope, target);
            } else {
                throw new BadRequestException("MEDIA_SCOPE", "Invalid scope");
            }
        }
        return ms.stream().map(this::meta).toList();
    }

    public record Download(UUID key, String filename, String mime, long size) {

    }

    public Download download(UUID actor, UUID id) {
        var m = one(id);
        switch (m.getScope()) {
            case "AVATAR" -> {
                if (!m.getOwnerUserId().equals(actor)) {
                    throw new AccessDeniedException("Avatar owner required");
                }
                policy.active(actor);
            }
            case "ORGANIZATION" ->
                policy.adminOrOrganizer(
                        actor,
                        m.getOrganizationId()
                );
            case "RESOURCE" ->
                policy.readResource(actor, m.getResourceId());
            case "LIBRARY" ->
                library.requireRead(actor, m.getLibraryResourceId());
            case "SUBMISSION" ->
                submissions.requireRead(actor, m.getSubmissionId());
            default ->
                throw new AccessDeniedException("File unavailable");
        }
        return new Download(
                m.getStorageKey(),
                m.getOriginalName(),
                m.getMimeType(),
                m.getSizeBytes()
        );
    }

    @Transactional
    public void remove(UUID actor, UUID id) {
        policy.active(actor);
        var asset = one(id);
        switch (asset.getScope()) {
            case "ORGANIZATION" ->
                policy.removeOrganizationDocument(
                        actor,
                        asset.getOrganizationId(),
                        asset.getId()
                );
            case "RESOURCE" ->
                policy.draftResource(actor, asset.getResourceId());
            case "LIBRARY" ->
                library.requireWrite(actor, asset.getLibraryResourceId());
            case "SUBMISSION" ->
                submissions.requireWrite(actor, asset.getSubmissionId());
            default ->
                throw new org.springframework.security.access.AccessDeniedException(
                        "Avatar removal is not supported here"
                );
        }
        if ("ORGANIZATION".equals(asset.getScope())
                && policy.retainOrganizationDocument(asset.getOrganizationId(), asset.getId())) {
            // Remove from the editable attachment set, retaining historical snapshot bytes.
            asset.setApplicationAttachmentActive(false);
            repo.save(asset);
            return;
        }
        repo.delete(asset);
        repo.flush();
        // Queue every reference removal. Counting inside concurrent delete transactions
        // could make both transactions miss the final-reference cleanup.
        cleanup.save(new MediaCleanupTask(asset.getStorageKey()));
    }

    public InputStream open(UUID key) {
        return objects.open(key);
    }

    @Transactional
    public void deleteForResources(Collection<UUID> ids) {
        if (ids != null) {
            ids.forEach(id
                    -> repo.findByResourceId(id).forEach(asset -> {
                        repo.delete(asset);
                        cleanup.save(new MediaCleanupTask(asset.getStorageKey()));
                    })
            );
        }
    }

    @Transactional
    public void cloneResourceAssets(Map<UUID, UUID> map) {
        map.forEach((s, t)
                -> repo.findByResourceId(s).forEach(a -> {
                    var object = storage.lock(a.getStorageKey()).orElseThrow();
                    if (object.getDeleted()) {
                        throw new StorageException("STORAGE_DELETED", "Cannot clone deleted storage bytes", null);
                    }
                    var n = new MediaAssetEntity();
                    n.setId(UUID.randomUUID());
                    n.setOwnerUserId(a.getOwnerUserId());
                    n.setScope("RESOURCE");
                    n.setResourceId(t);
                    n.setOriginalName(a.getOriginalName());
                    n.setStorageKey(a.getStorageKey());
                    n.setMimeType(a.getMimeType());
                    n.setSizeBytes(a.getSizeBytes());
                    n.setSha256(a.getSha256());
                    n.setCreatedAt(Instant.now());
                    repo.save(n);
                })
        );
    }

    public boolean hasAssetsForAll(Collection<UUID> ids) {
        return (ids == null
                || ids.isEmpty()
                || ids.stream().allMatch(id -> repo.countByResourceId(id) > 0));
    }

    @Override
    public List<UUID> documentIds(UUID organizationId) {
        return repo
                .findByScopeAndOrganizationIdAndApplicationAttachmentActiveTrueOrderByCreatedAtDesc(
                        "ORGANIZATION",
                        organizationId
                )
                .stream()
                .map(asset -> asset.getId())
                .toList();
    }

    @Override
    public boolean hasSubmissionFiles(UUID id) {
        return repo.countBySubmissionId(id) > 0;
    }

    @Override
    public boolean hasLibraryFiles(UUID id) {
        return repo.countByLibraryResourceId(id) > 0;
    }

}
