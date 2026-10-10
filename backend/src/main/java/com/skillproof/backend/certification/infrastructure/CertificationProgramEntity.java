package com.skillproof.backend.certification.infrastructure;

import java.time.Instant;
import java.util.UUID;

import com.skillproof.backend.certification.domain.CertificationProgram;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "certification_program")
class CertificationProgramEntity {

    @Id
    UUID id;
    @Column(name = "organization_id", nullable = false)
    UUID organizationId;
    @Column(name = "course_version_id", nullable = false)
    UUID courseVersionId;
    @Column(name = "completion_policy_id", nullable = false)
    UUID completionPolicyId;
    @Column(nullable = false)
    String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CertificationProgram.Status status;
    @Column(name = "created_by_user_id", nullable = false)
    UUID createdByUserId;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    protected CertificationProgramEntity() {
    }

    CertificationProgramEntity(CertificationProgram p) {
        id = p.id();
        organizationId = p.organizationId();
        courseVersionId = p.courseVersionId();
        completionPolicyId = p.completionPolicyId();
        name = p.name();
        status = p.status();
        createdByUserId = p.createdByUserId();
        createdAt = p.createdAt();
    }

    CertificationProgram domain() {
        return new CertificationProgram(id, organizationId, courseVersionId, completionPolicyId,
                name, status, createdByUserId, createdAt);
    }
}
