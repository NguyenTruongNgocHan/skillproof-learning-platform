package com.skillproof.backend.certification.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.skillproof.backend.certification.domain.CertificateEligibility;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "certificate_eligibility")
class CertificateEligibilityEntity {

    @Id
    UUID id;
    @Column(name = "certification_program_id", nullable = false)
    UUID certificationProgramId;
    @Column(name = "learner_user_id", nullable = false)
    UUID learnerUserId;
    @Column(name = "enrollment_id", nullable = false)
    UUID enrollmentId;
    @Column(name = "completion_evaluation_id")
    UUID completionEvaluationId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CertificateEligibility.Status status;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence_snapshot_json", nullable = false, columnDefinition = "jsonb")
    String evidenceSnapshotJson;
    @Column(name = "evaluated_at", nullable = false)
    Instant evaluatedAt;

    protected CertificateEligibilityEntity() {
    }

    CertificateEligibilityEntity(CertificateEligibility e) {
        id = e.id();
        certificationProgramId = e.certificationProgramId();
        learnerUserId = e.learnerUserId();
        enrollmentId = e.enrollmentId();
        completionEvaluationId = e.completionEvaluationId();
        status = e.status();
        evidenceSnapshotJson = e.evidenceSnapshotJson();
        evaluatedAt = e.evaluatedAt();
    }

    CertificateEligibility domain() {
        return new CertificateEligibility(id, certificationProgramId, learnerUserId, enrollmentId,
                completionEvaluationId, status, evidenceSnapshotJson, evaluatedAt);
    }
}
