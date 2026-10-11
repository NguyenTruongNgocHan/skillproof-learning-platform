package com.skillproof.backend.media.application;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.media.application.port.*;
import com.skillproof.backend.media.infrastructure.*;
import com.skillproof.backend.media.infrastructure.cleanup.*;
import com.skillproof.backend.media.infrastructure.persistence.*;
import java.io.*;
import java.time.*;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DirectUploadService {
  private final DirectUploadRepository tickets;
  private final DirectObjectUpload direct;
  private final MediaObjectStore objects;
  private final MediaService media;
  private final StorageObjectRepository storage;
  private final MediaCleanupRepository cleanup;

  public DirectUploadService(
      DirectUploadRepository tickets,
      DirectObjectUpload direct,
      MediaObjectStore objects,
      MediaService media,
      StorageObjectRepository storage,
      MediaCleanupRepository cleanup) {
    this.tickets = tickets;
    this.direct = direct;
    this.objects = objects;
    this.media = media;
    this.storage = storage;
    this.cleanup = cleanup;
  }

  @Transactional
  public Map<String, Object> prepare(
      UUID actor, String scope, UUID target, String name, long size) {
    media.requireUploadAccess(actor, scope, target);
    if (!Set.of(
                "RESOURCE",
                "LIBRARY",
                "SUBMISSION",
                "ORGANIZATION",
                "ISSUER",
                "BRAND",
                "COURSE_COVER")
            .contains(scope)
        || name == null
        || name.isBlank()
        || name.length() > 180
        || size < 1
        || size > 524288000L)
      throw new BadRequestException("UPLOAD_INVALID", "Supported file up to 500 MB required");
    if (Set.of("BRAND", "COURSE_COVER").contains(scope) && size > 10485760L)
      throw new BadRequestException("BRAND_SIZE", "Image must be up to 10 MB");
    UUID id = UUID.randomUUID(), key = UUID.randomUUID();
    Instant expires = Instant.now().plusSeconds(size > 33554432L ? 3600 : 600);
    storage.save(new StorageObjectEntity(key, false, Instant.now()));
    var task = new MediaCleanupTask(key);
    task.setNextAttemptAt(expires.plusSeconds(300));
    cleanup.save(task);
    tickets.save(new DirectUploadTicket(id, actor, scope, target, key, name, size, expires, null));
    return Map.of("id", id, "url", direct.signedPut(key), "expiresAt", expires);
  }

  @Transactional(timeout = 600)
  public Map<String, Object> complete(UUID actor, UUID id) {
    var t = tickets.lock(id).orElseThrow();
    if (!actor.equals(t.getOwnerId())) throw new AccessDeniedException("Upload owner required");
    if (t.isCancelled()) throw new ConflictException("UPLOAD_CANCELLED", "Upload was cancelled");
    if (t.getAssetId() != null) {
      media.download(actor, t.getAssetId());
      return media.metadata(t.getAssetId());
    }
    if (t.getMultipartId() != null && !t.isAssembled())
      throw new ConflictException(
          "UPLOAD_NOT_ASSEMBLED", "Assemble the multipart upload before finalizing");
    if (!t.getExpiresAt().isAfter(Instant.now()))
      throw new ConflictException("UPLOAD_EXPIRED", "Prepare a new upload");
    var temporary = storage.lock(t.getStorageKey()).orElseThrow();
    if (temporary.getDeleted())
      throw new ConflictException("UPLOAD_CLEANED", "Prepare a new upload");
    long size = direct.size(t.getStorageKey());
    if (size != t.getExpectedSize() || size > 524288000L)
      throw new BadRequestException("UPLOAD_SIZE", "Object size mismatch");
    MultipartFile file =
        new MultipartFile() {
          public String getName() {
            return "file";
          }

          public String getOriginalFilename() {
            return t.getFilename();
          }

          public String getContentType() {
            return "application/octet-stream";
          }

          public boolean isEmpty() {
            return size == 0;
          }

          public long getSize() {
            return size;
          }

          public byte[] getBytes() {
            throw new UnsupportedOperationException("Stream only");
          }

          public InputStream getInputStream() {
            return objects.open(t.getStorageKey());
          }

          public void transferTo(File f) {
            throw new UnsupportedOperationException("Local persistence is disabled");
          }
        };
    var result = media.upload(actor, t.getScope(), t.getTargetId(), file);
    t.complete((UUID) result.get("id"));
    return result;
  }
}
