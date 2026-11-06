package com.skillproof.backend.analytics.infrastructure;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "learning_signal")
public class LearningSignal {
  @Id private UUID id;

  @Column(name = "learner_id", nullable = false)
  private UUID learnerId;

  @Column(name = "course_version_id", nullable = false)
  private UUID courseVersionId;

  @Column(nullable = false, length = 32)
  private String kind;

  @Column(name = "reference_id", nullable = false)
  private UUID referenceId;

  @Column(name = "schema_version", nullable = false)
  private int schemaVersion = 1;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> details;

  protected LearningSignal() {}

  public LearningSignal(
      UUID id,
      UUID learner,
      UUID version,
      String kind,
      UUID reference,
      Map<String, Object> details) {
    this.id = id;
    learnerId = learner;
    courseVersionId = version;
    this.kind = kind;
    referenceId = reference;
    occurredAt = Instant.now();
    this.details = Map.copyOf(details);
  }
}
