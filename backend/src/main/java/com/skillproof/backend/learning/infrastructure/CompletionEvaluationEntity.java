package com.skillproof.backend.learning.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "completion_evaluation")
public class CompletionEvaluationEntity {

    @Id
    private UUID id;

    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;

    @Column(name = "learning_path_version_id", nullable = false)
    private UUID learningPathVersionId;

    @Column(nullable = false, length = 24)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence_snapshot_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode evidenceSnapshotJson;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    protected CompletionEvaluationEntity() {
    }

    CompletionEvaluationEntity(UUID id, UUID enrollmentId, UUID learningPathVersionId,
            String status, JsonNode evidenceSnapshotJson, Instant evaluatedAt) {
        this.id = id;
        this.enrollmentId = enrollmentId;
        this.learningPathVersionId = learningPathVersionId;
        this.status = status;
        this.evidenceSnapshotJson = evidenceSnapshotJson;
        this.evaluatedAt = evaluatedAt;
    }

    public JsonNode getEvidenceSnapshotJson() {
        return evidenceSnapshotJson;
    }
}
