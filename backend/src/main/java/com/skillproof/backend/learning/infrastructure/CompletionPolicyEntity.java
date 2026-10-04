package com.skillproof.backend.learning.infrastructure;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "completion_policy")
public class CompletionPolicyEntity {

    @Id
    @Column(name = "version_id")
    private UUID versionId;

    @Column(name = "id", insertable = false, updatable = false)
    private UUID logicalId;

    @Column(name = "require_all_resources", nullable = false)
    private boolean requireAllResources;

    @Column(name = "require_official_assessments", nullable = false)
    private boolean requireOfficialAssessments;

    protected CompletionPolicyEntity() {
    }

    public CompletionPolicyEntity(UUID versionId, boolean resources, boolean assessments) {
        this.versionId = versionId;
        this.requireAllResources = resources;
        this.requireOfficialAssessments = assessments;
    }

    public UUID getLogicalId() {
        return logicalId;
    }

    public boolean isRequireAllResources() {
        return requireAllResources;
    }

    public boolean isRequireOfficialAssessments() {
        return requireOfficialAssessments;
    }

    public void configure(boolean resources, boolean assessments) {
        this.requireAllResources = resources;
        this.requireOfficialAssessments = assessments;
    }
}
