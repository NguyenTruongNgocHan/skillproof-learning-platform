package com.skillproof.backend.assignment.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assignment")
public class AssignmentEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "version_id", nullable = false)
    private UUID versionId;

    @Column(name = "owner_scope", nullable = false, length = 12)
    private String ownerScope;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "instructions", nullable = false, columnDefinition = "text")
    private String instructions;

    @Column(name = "required", nullable = false)
    private boolean required;

    @Column(name = "pass_percent", nullable = false)
    private int passPercent;

    @Column(name = "max_submissions", nullable = false)
    private int maxSubmissions;

    @Column(name = "due_at", nullable = true)
    private Instant dueAt;

    protected AssignmentEntity() {
    }

    public AssignmentEntity(UUID id, UUID versionId, String ownerScope, UUID ownerId, String title, String instructions, boolean required, int passPercent, int maxSubmissions, Instant dueAt) {
        this.id = id;
        this.versionId = versionId;
        this.ownerScope = ownerScope;
        this.ownerId = ownerId;
        this.title = title;
        this.instructions = instructions;
        this.required = required;
        this.passPercent = passPercent;
        this.maxSubmissions = maxSubmissions;
        this.dueAt = dueAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVersionId() {
        return versionId;
    }

    public String getOwnerScope() {
        return ownerScope;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructions() {
        return instructions;
    }

    public boolean getRequired() {
        return required;
    }

    public int getPassPercent() {
        return passPercent;
    }

    public int getMaxSubmissions() {
        return maxSubmissions;
    }

    public Instant getDueAt() {
        return dueAt;
    }

}
