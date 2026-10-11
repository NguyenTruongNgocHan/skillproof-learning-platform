package com.skillproof.backend.media;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.skillproof.backend.assignment.contract.AssignmentMediaAccess;
import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.course.contract.CourseResourceAccessQuery;
import com.skillproof.backend.library.contract.LibraryMediaAccess;
import com.skillproof.backend.media.application.*;
import com.skillproof.backend.media.application.port.MediaObjectStore;
import com.skillproof.backend.media.infrastructure.cleanup.*;
import com.skillproof.backend.media.infrastructure.persistence.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class MediaStreamIntegrityTest {
  private final MediaAssetRepository assets = mock(MediaAssetRepository.class);
  private final MediaObjectStore objects = mock(MediaObjectStore.class);
  private final StorageObjectRepository storage = mock(StorageObjectRepository.class);
  private final MediaService service =
      new MediaService(
          assets,
          objects,
          mock(MediaAccessPolicy.class),
          mock(CourseResourceAccessQuery.class),
          mock(MediaCleanupRepository.class),
          mock(MediaCleanupReservation.class),
          mock(AssignmentMediaAccess.class),
          mock(LibraryMediaAccess.class),
          storage);
  private final byte[] png =
      new byte[] {(byte) 137, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 0, 0, 0, 0, 0};

  @BeforeEach
  void fixture() throws Exception {
    when(storage.lock(any()))
        .thenAnswer(
            i -> Optional.of(new StorageObjectEntity(i.getArgument(0), false, Instant.now())));
    doAnswer(
            i -> {
              ((InputStream) i.getArgument(1)).readAllBytes();
              return null;
            })
        .when(objects)
        .put(any(), any(), anyLong(), anyString());
    when(assets.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
  }

  @Test
  void storedStreamMustMatchValidatedStream() {
    var file =
        new MockMultipartFile("file", "image.png", "image/png", png) {
          private int reads;

          @Override
          public InputStream getInputStream() {
            var bytes = png.clone();
            if (++reads > 1) bytes[15] = 1;
            return new ByteArrayInputStream(bytes);
          }
        };
    assertThatThrownBy(() -> service.upload(java.util.UUID.randomUUID(), "AVATAR", null, file))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("changed");
    verify(assets, never()).saveAndFlush(any());
  }

  @Test
  void stableStreamProducesAHashAndMetadata() {
    var result =
        service.upload(
            java.util.UUID.randomUUID(),
            "AVATAR",
            null,
            new MockMultipartFile("file", "image.png", "image/png", png));
    assertThat(result.get("sha256").toString()).hasSize(64);
    assertThat(result.get("size")).isEqualTo(16L);
  }
}
