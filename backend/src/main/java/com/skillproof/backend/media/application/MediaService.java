package com.skillproof.backend.media.application;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CourseMediaDependency;
import com.skillproof.backend.course.contract.CourseResourceAccessQuery;
import com.skillproof.backend.media.application.exception.StorageException;
import com.skillproof.backend.media.application.port.MediaObjectStore;
import com.skillproof.backend.media.contract.MediaAvatarQuery;
import com.skillproof.backend.media.domain.MediaSignature;
import com.skillproof.backend.media.infrastructure.cleanup.MediaCleanupRepository;
import com.skillproof.backend.media.infrastructure.cleanup.MediaCleanupReservation;
import com.skillproof.backend.media.infrastructure.cleanup.MediaCleanupTask;
import com.skillproof.backend.media.infrastructure.persistence.MediaAssetEntity;
import com.skillproof.backend.media.infrastructure.persistence.MediaAssetRepository;
import com.skillproof.backend.organization.contract.OrganizationDocumentQuery;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaService
    implements CourseMediaDependency,
        MediaAvatarQuery,
        OrganizationDocumentQuery,
        com.skillproof.backend.media.contract.SubmissionMediaQuery,
        com.skillproof.backend.media.contract.LibraryMediaQuery {
  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.course.contract.CourseWorkspaceQuery workspaceCourses;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.feedback.contract.ExperienceJournal experiences;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.identity.contract.IdentityAccessQuery identity;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.organization.contract.OrganizationAuthorityQuery orgGrants;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.organization.contract.OrganizationPublicQuery orgs;

  private void issuerWrite(UUID actor, UUID org) {
    if (org == null || !orgGrants.hasAuthority(org, actor, "MANAGE_PROFILE"))
      throw new AccessDeniedException("Profile authority required");
    orgs.requireApprovedForUpdate(org);
  }

  private final com.skillproof.backend.assignment.contract.AssignmentMediaAccess submissions;
  private final com.skillproof.backend.library.contract.LibraryMediaAccess library;
  private final com.skillproof.backend.media.infrastructure.persistence.StorageObjectRepository
      storage;
  private final MediaAssetRepository repo;
  private final MediaCleanupReservation reservations;
  private final MediaCleanupRepository cleanup;
  private final MediaObjectStore objects;
  private final MediaAccessPolicy policy;
  private final CourseResourceAccessQuery resources;

  public void requireUploadAccess(UUID actor, String scope, UUID target) {
    policy.active(actor);
    switch (scope) {
      case "ORGANIZATION" -> policy.writeOrganization(actor, target);
      case "ISSUER", "BRAND" -> issuerWrite(actor, target);
      case "COURSE_COVER" -> workspaceCourses.requireCourseAuthor(actor, target);
      case "RESOURCE" -> policy.draftResource(actor, target);
      case "LIBRARY" -> library.requireWrite(actor, target);
      case "SUBMISSION" -> submissions.requireWrite(actor, target);
      default -> throw new BadRequestException("MEDIA_SCOPE", "Unsupported direct upload scope");
    }
  }

  private static final long LIMIT = 500L * 1024 * 1024;

  public MediaService(
      MediaAssetRepository r,
      MediaObjectStore o,
      MediaAccessPolicy p,
      CourseResourceAccessQuery q,
      MediaCleanupRepository cleanup,
      MediaCleanupReservation reservations,
      com.skillproof.backend.assignment.contract.AssignmentMediaAccess submissions,
      com.skillproof.backend.library.contract.LibraryMediaAccess library,
      com.skillproof.backend.media.infrastructure.persistence.StorageObjectRepository storage) {
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
    return repo.findById(id)
        .orElseThrow(() -> new NotFoundException("MEDIA_NOT_FOUND", "File not found"));
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
        m.getCreatedAt(),
        "sha256",
        m.getSha256() == null ? "" : m.getSha256());
  }

  @Transactional(timeout = 600)
  public Map<String, Object> upload(UUID actor, String scope, UUID target, MultipartFile file) {
    policy.active(actor);
    if (file == null || file.isEmpty() || file.getSize() > LIMIT) {
      throw new BadRequestException("MEDIA_SIZE", "File must contain 1 to 500 MB");
    }
    if (scope.equals("AVATAR") && target != null) {
      throw new BadRequestException("MEDIA_TARGET", "Avatar has no target");
    }
    if (scope.equals("ORGANIZATION")) {
      if (target == null) {
        throw new BadRequestException("MEDIA_TARGET", "Organization required");
      }
      policy.writeOrganization(actor, target);
    }
    if (scope.equals("ISSUER") || scope.equals("BRAND")) issuerWrite(actor, target);
    if (scope.equals("COURSE_COVER")) workspaceCourses.requireCourseAuthor(actor, target);
    if (scope.equals("RESOURCE")) {
      if (target == null) {
        throw new BadRequestException("MEDIA_TARGET", "Resource required");
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
    if (!Set.of(
            "AVATAR",
            "ORGANIZATION",
            "ISSUER",
            "BRAND",
            "COURSE_COVER",
            "RESOURCE",
            "LIBRARY",
            "SUBMISSION")
        .contains(scope)) {
      throw new BadRequestException("MEDIA_SCOPE", "Invalid media scope");
    }
    String name =
        Optional.ofNullable(file.getOriginalFilename())
            .orElse("file")
            .replaceAll("[\\p{Cntrl}/\\\\]", "_");
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
            throw new BadRequestException("MEDIA_SIZE", "File exceeds 500 MB");
          }
          if (h < head.length) {
            int x = Math.min(n, head.length - h);
            System.arraycopy(buf, 0, head, h, x);
            h += x;
          }
          sha.update(buf, 0, n);
        }
      }
      if (count == 0 || count != file.getSize()) {
        throw new BadRequestException("MEDIA_SIZE", "File size changed during validation");
      }
      byte[] expectedHash = sha.digest();
      String mime = MediaSignature.detect(Arrays.copyOf(head, h));
      if (scope.equals("AVATAR") && !mime.startsWith("image/")) {
        throw new BadRequestException("MEDIA_TYPE", "Avatar must be JPEG, PNG or WebP");
      }
      if ((scope.equals("ORGANIZATION") || scope.equals("ISSUER"))
          && !mime.equals("application/pdf")
          && !mime.startsWith("image/")) {
        throw new BadRequestException("MEDIA_TYPE", "Organization document must be PDF or image");
      }
      if (scope.equals("RESOURCE")
          && (("VIDEO".equals(resources.resourceKind(target)) && !mime.startsWith("video/"))
              || ("AUDIO".equals(resources.resourceKind(target)) && !mime.startsWith("audio/"))
              || ("IMAGE".equals(resources.resourceKind(target)) && !mime.startsWith("image/")))) {
        throw new BadRequestException("MEDIA_TYPE", "Audio lessons require an audio file");
      }
      if (("BRAND".equals(scope) || "COURSE_COVER".equals(scope))
          && (!mime.startsWith("image/") || count > 10485760L))
        throw new BadRequestException("BRAND_IMAGE", "Use a JPEG, PNG or WebP image up to 10 MB");
      // Persist cleanup independently before creating external bytes.
      // Worker waits for upload transaction to finish and retains referenced bytes.
      reservations.reserve(key);
      storage.lock(key).orElseThrow();
      MessageDigest copiedHash = MessageDigest.getInstance("SHA-256");
      try (InputStream in =
          new java.security.DigestInputStream(file.getInputStream(), copiedHash)) {
        objects.put(key, in, count, mime);
      }
      if (!MessageDigest.isEqual(expectedHash, copiedHash.digest())) {
        throw new BadRequestException(
            "MEDIA_CHANGED", "File changed during upload; retry with a stable file");
      }
      var m = new MediaAssetEntity();
      m.setId(id);
      m.setOwnerUserId(actor);
      m.setScope(scope);
      m.setOrganizationId(
          (scope.equals("ORGANIZATION") || scope.equals("ISSUER") || scope.equals("BRAND"))
              ? target
              : null);
      m.setCourseId(scope.equals("COURSE_COVER") ? target : null);
      m.setResourceId(scope.equals("RESOURCE") ? target : null);
      m.setLibraryResourceId(scope.equals("LIBRARY") ? target : null);
      m.setSubmissionId(scope.equals("SUBMISSION") ? target : null);
      m.setOriginalName(name);
      m.setStorageKey(key);
      m.setMimeType(mime);
      m.setSizeBytes(count);
      m.setSha256(HexFormat.of().formatHex(expectedHash));
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
      throw new AccessDeniedException("Avatar must belong to your account");
    }
  }

  public Map<String, Object> metadata(UUID id) {
    return meta(one(id));
  }

  public List<Map<String, Object>> list(UUID actor, String scope, UUID target) {
    List<MediaAssetEntity> ms;
    if (scope.equals("AVATAR")) {
      policy.active(actor);
      ms = repo.findByScopeAndOwnerUserIdOrderByCreatedAtDesc(scope, actor);
    } else {
      if (target == null) {
        throw new BadRequestException("MEDIA_TARGET", "Target required");
      }
      if (scope.equals("ORGANIZATION") || scope.equals("ISSUER") || scope.equals("BRAND")) {
        policy.adminOrOrganizer(actor, target);
        ms =
            repo.findByScopeAndOrganizationIdAndApplicationAttachmentActiveTrueOrderByCreatedAtDesc(
                scope, target);
      } else if (scope.equals("COURSE_COVER")) {
        workspaceCourses.requireCourseAuthor(actor, target);
        ms = repo.findByScopeAndCourseIdOrderByCreatedAtDesc(scope, target);
      } else if (scope.equals("RESOURCE")) {
        policy.readResource(actor, target);
        ms = repo.findByScopeAndResourceIdOrderByCreatedAtDesc(scope, target);
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

  public record Download(UUID key, String filename, String mime, long size) {}

  @org.springframework.transaction.annotation.Transactional
  public Download download(UUID actor, UUID id) {
    var m = one(id);
    switch (m.getScope()) {
      case "AVATAR" -> {
        if (!m.getOwnerUserId().equals(actor)) {
          throw new AccessDeniedException("Avatar owner required");
        }
        policy.active(actor);
      }
      case "ORGANIZATION", "ISSUER", "BRAND" ->
          policy.adminOrOrganizer(actor, m.getOrganizationId());
      case "COURSE_COVER" -> workspaceCourses.requireCourseAuthor(actor, m.getCourseId());
      case "RESOURCE" -> policy.readResource(actor, m.getResourceId());
      case "LIBRARY" -> {
        library.requireRead(actor, m.getLibraryResourceId());
        if (identity.isActiveLearner(actor))
          experiences.accessed(actor, "RESOURCE", m.getLibraryResourceId());
      }
      case "SUBMISSION" -> submissions.requireRead(actor, m.getSubmissionId());
      default -> throw new AccessDeniedException("File unavailable");
    }
    return new Download(m.getStorageKey(), m.getOriginalName(), m.getMimeType(), m.getSizeBytes());
  }

  @Transactional
  public void remove(UUID actor, UUID id) {
    policy.active(actor);
    var asset = one(id);
    if (Set.of("ISSUER", "BRAND", "COURSE_COVER").contains(asset.getScope()))
      throw new AccessDeniedException(
          "Issuer evidence is retained for audit; upload a new document instead");
    switch (asset.getScope()) {
      case "ORGANIZATION" ->
          policy.removeOrganizationDocument(actor, asset.getOrganizationId(), asset.getId());
      case "RESOURCE" -> policy.draftResource(actor, asset.getResourceId());
      case "LIBRARY" -> library.requireWrite(actor, asset.getLibraryResourceId());
      case "SUBMISSION" -> submissions.requireWrite(actor, asset.getSubmissionId());
      default ->
          throw new org.springframework.security.access.AccessDeniedException(
              "Avatar removal is not supported here");
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
      ids.forEach(
          id ->
              repo.findByResourceId(id)
                  .forEach(
                      asset -> {
                        repo.delete(asset);
                        cleanup.save(new MediaCleanupTask(asset.getStorageKey()));
                      }));
    }
  }

  @Transactional
  public void cloneResourceAssets(Map<UUID, UUID> map) {
    map.forEach(
        (s, t) ->
            repo.findByResourceId(s)
                .forEach(
                    a -> {
                      var object = storage.lock(a.getStorageKey()).orElseThrow();
                      if (object.getDeleted()) {
                        throw new StorageException(
                            "STORAGE_DELETED", "Cannot clone deleted storage bytes", null);
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
                    }));
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
            "ORGANIZATION", organizationId)
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

  @Override
  public boolean hasMatchingAsset(UUID resourceId, String kind) {
    return repo.findByScopeAndResourceIdOrderByCreatedAtDesc("RESOURCE", resourceId).stream()
        .anyMatch(
            f ->
                "FILE".equals(kind)
                    || ("IMAGE".equals(kind) && f.getMimeType().startsWith("image/"))
                    || ("AUDIO".equals(kind) && f.getMimeType().startsWith("audio/"))
                    || ("VIDEO".equals(kind) && f.getMimeType().startsWith("video/")));
  }
}
