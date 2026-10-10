package com.skillproof.backend.course.infrastructure;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "completion_evaluation")
public class CompletionEvaluationEntity {

    @Id
    private UUID id;

    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;

    @Column(name = "course_version_id", nullable = false)
    private UUID courseVersionId;

    @Column(nullable = false, length = 24)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "evidence_snapshot_json",
            nullable = false,
            columnDefinition = "jsonb"
    )
    private Map<String, Object> evidenceSnapshotJson;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    protected CompletionEvaluationEntity() {
    }

    CompletionEvaluationEntity(
            UUID id,
            UUID enrollmentId,
            UUID courseVersionId,
            String status,
            Map<String, Object> evidenceSnapshotJson,
            Instant evaluatedAt
    ) {
        this.id = id;
        this.enrollmentId = enrollmentId;
        this.courseVersionId = courseVersionId;
        this.status = status;
        this.evidenceSnapshotJson = evidenceSnapshotJson;
        this.evaluatedAt = evaluatedAt;
    }

    public Map<String, Object> getEvidenceSnapshotJson() {
        return evidenceSnapshotJson;
    }
}