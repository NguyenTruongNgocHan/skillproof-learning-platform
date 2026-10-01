package com.skillproof.backend.media;

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
import com.skillproof.backend.learning.contract.LearningMediaDependency;
import com.skillproof.backend.learning.contract.LearningResourceAccessQuery;
import com.skillproof.backend.media.contract.MediaAvatarQuery;

import jakarta.transaction.Transactional;

@Service
public class MediaService implements LearningMediaDependency, MediaAvatarQuery {

    private final MediaAssetRepository repo;
    private final MediaObjectStore objects;
    private final MediaAccessPolicy policy;
    private final LearningResourceAccessQuery resources;
    private static final long LIMIT = 100L * 1024 * 1024;

    public MediaService(MediaAssetRepository r, MediaObjectStore o, MediaAccessPolicy p, LearningResourceAccessQuery q) {
        repo = r;
        objects = o;
        policy = p;
        resources = q;
    }

    private MediaAsset one(UUID id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("MEDIA_NOT_FOUND", "File not found"));
    }

    private Map<String, Object> meta(MediaAsset m) {
        return Map.of("id", m.id, "name", m.originalName, "mimeType", m.mimeType, "size", m.sizeBytes, "createdAt", m.createdAt);
    }

    public Map<String, Object> upload(UUID actor, String scope, UUID target, MultipartFile file) {
        policy.active(actor);
        if (file == null || file.isEmpty() || file.getSize() > LIMIT) {
            throw new BadRequestException("MEDIA_SIZE", "File must contain 1 to 100 MB");
        }
        if (scope.equals("AVATAR") && target != null) {
            throw new BadRequestException("MEDIA_TARGET", "Avatar has no target");
        }
        if (scope.equals("ORGANIZATION")) {
            if (target == null) {
                throw new BadRequestException("MEDIA_TARGET", "Organization required");
            
            }policy.adminOrOrganizer(actor, target);
        }
        if (scope.equals("RESOURCE")) {
            if (target == null) {
                throw new BadRequestException("MEDIA_TARGET", "Resource required");
            
            }policy.draftResource(actor, target);
        }
        if (!Set.of("AVATAR", "ORGANIZATION", "RESOURCE").contains(scope)) {
            throw new BadRequestException("MEDIA_SCOPE", "Invalid media scope");
        }
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("file").replaceAll("[\\p{Cntrl}/\\\\]", "_");
        if (name.isBlank() || name.length() > 180) {
            throw new BadRequestException("MEDIA_NAME", "Invalid filename");
        }
        UUID id = UUID.randomUUID(), key = UUID.randomUUID();
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
                        throw new BadRequestException("MEDIA_SIZE", "File exceeds 100 MB");
                    
                    }if (h < head.length) {
                        int x = Math.min(n, head.length - h);
                        System.arraycopy(buf, 0, head, h, x);
                        h += x;
                    }
                    sha.update(buf, 0, n);
                }
            }
            if (count == 0) {
                throw new BadRequestException("MEDIA_SIZE", "Empty file");
            
            }String mime = MediaSignature.detect(Arrays.copyOf(head, h));
            if (scope.equals("AVATAR") && !mime.startsWith("image/")) {
                throw new BadRequestException("MEDIA_TYPE", "Avatar must be JPEG, PNG or WebP");
            
            }if (scope.equals("ORGANIZATION") && !mime.equals("application/pdf") && !mime.startsWith("image/")) {
                throw new BadRequestException("MEDIA_TYPE", "Organization document must be PDF or image");
            
            }if (scope.equals("RESOURCE") && "AUDIO".equals(resources.resourceKind(target)) && !mime.startsWith("audio/")) {
                throw new BadRequestException("MEDIA_TYPE", "Audio lessons require an audio file");
            
            }try (InputStream in = file.getInputStream()) {
                objects.put(key, in, count, mime);
            }
            var m = new MediaAsset();
            m.id = id;
            m.ownerUserId = actor;
            m.scope = scope;
            m.organizationId = scope.equals("ORGANIZATION") ? target : null;
            m.resourceId = scope.equals("RESOURCE") ? target : null;
            m.originalName = name;
            m.storageKey = key;
            m.mimeType = mime;
            m.sizeBytes = count;
            m.sha256 = HexFormat.of().formatHex(sha.digest());
            m.createdAt = Instant.now();
            try {
                return meta(repo.save(m));
            } catch (RuntimeException e) {
                objects.delete(key);
                throw e;
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Could not store file", e);
        }
    }

    public void verifyAvatar(UUID actor, UUID id) {
        var m = one(id);
        if (!"AVATAR".equals(m.scope) || !actor.equals(m.ownerUserId)) {
            throw new AccessDeniedException("Avatar must belong to your account");
    
        }}

    public Map<String, Object> metadata(UUID id) {
        return meta(one(id));
    }

    public List<Map<String, Object>> list(UUID actor, String scope, UUID target) {
        List<MediaAsset> ms;
        if (scope.equals("AVATAR")) {
            policy.active(actor);
            ms = repo.findByScopeAndOwnerUserIdOrderByCreatedAtDesc(scope, actor);
        } else {
            if (target == null) {
                throw new BadRequestException("MEDIA_TARGET", "Target required");
            
            }if (scope.equals("ORGANIZATION")) {
                policy.adminOrOrganizer(actor, target);
                ms = repo.findByScopeAndOrganizationIdOrderByCreatedAtDesc(scope, target);
            } else if (scope.equals("RESOURCE")) {
                policy.readResource(actor, target);
                ms = repo.findByScopeAndResourceIdOrderByCreatedAtDesc(scope, target);
            } else {
                throw new BadRequestException("MEDIA_SCOPE", "Invalid scope");
        
            }}
        return ms.stream().map(this::meta).toList();
    }

    public record Download(UUID key, String filename, String mime, long size) {

    }

    public Download download(UUID actor, UUID id) {
        var m = one(id);
        switch (m.scope) {
            case "AVATAR" -> {
                if (!m.ownerUserId.equals(actor)) {
                    throw new AccessDeniedException("Avatar owner required");
                
                }policy.active(actor);
            }
            case "ORGANIZATION" ->
                policy.adminOrOrganizer(actor, m.organizationId);
            case "RESOURCE" ->
                policy.readResource(actor, m.resourceId);
            default ->
                throw new AccessDeniedException("File unavailable");
        }
        return new Download(m.storageKey, m.originalName, m.mimeType, m.sizeBytes);
    }

    public InputStream open(UUID key) {
        return objects.open(key);
    }

    @Transactional
    public void deleteForResources(Collection<UUID> ids) {
        if (ids != null) {
            ids.forEach(id -> repo.deleteAll(repo.findByResourceId(id)));
    
        }}

    @Transactional
    public void cloneResourceAssets(Map<UUID, UUID> map) {
        map.forEach((s, t) -> repo.findByResourceId(s).forEach(a -> {
            var n = new MediaAsset();
            n.id = UUID.randomUUID();
            n.ownerUserId = a.ownerUserId;
            n.scope = "RESOURCE";
            n.resourceId = t;
            n.originalName = a.originalName;
            n.storageKey = a.storageKey;
            n.mimeType = a.mimeType;
            n.sizeBytes = a.sizeBytes;
            n.sha256 = a.sha256;
            n.createdAt = Instant.now();
            repo.save(n);
        }));
    }

    public boolean hasAssetsForAll(Collection<UUID> ids) {
        return ids == null || ids.isEmpty() || ids.stream().allMatch(id -> repo.countByResourceId(id) > 0);
    }
}
