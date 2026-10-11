package com.skillproof.backend.operations.application;

import com.skillproof.backend.operations.contract.ActivityJournal;
import com.skillproof.backend.operations.infrastructure.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityJournalService implements ActivityJournal {
  private final AuditRepository audit;
  private final NoticeRepository notices;

  public ActivityJournalService(AuditRepository audit, NoticeRepository notices) {
    this.audit = audit;
    this.notices = notices;
  }

  @Transactional
  public void record(UUID actor, UUID org, String action, UUID target, String details) {
    audit.save(
        new AuditEntry(UUID.randomUUID(), actor, org, action, target, details, Instant.now()));
  }

  @Transactional
  public void notify(UUID actor, UUID org, String category, String message, String link) {
    notices.save(
        new WorkspaceNotice(
            UUID.randomUUID(),
            actor,
            org,
            category,
            message.length() > 1000 ? message.substring(0, 997) + "..." : message,
            link,
            Instant.now(),
            null));
  }
}
