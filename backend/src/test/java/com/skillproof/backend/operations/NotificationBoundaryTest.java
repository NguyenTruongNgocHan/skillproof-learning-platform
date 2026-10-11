package com.skillproof.backend.operations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.skillproof.backend.operations.application.ActivityJournalService;
import com.skillproof.backend.operations.infrastructure.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NotificationBoundaryTest {
  @Test
  void maximumReviewReasonCannotOverflowNotificationColumn() {
    var notices = mock(NoticeRepository.class);
    var service = new ActivityJournalService(mock(AuditRepository.class), notices);
    service.notify(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "APPLICATION",
        "NEEDS_INFORMATION: " + "x".repeat(1000),
        "/organizer/verification-pending");
    var message = ArgumentCaptor.forClass(WorkspaceNotice.class);
    verify(notices).save(message.capture());
    assertThat(message.getValue().getMessage()).hasSize(1000).endsWith("...");
  }
}
