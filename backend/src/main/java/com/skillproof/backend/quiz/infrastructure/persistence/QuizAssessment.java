package com.skillproof.backend.quiz.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment")
public class QuizAssessment {

    @Id
    private UUID id;
    @Column(name = "organization_id")
    private UUID organizationId;
    @Column(name = "version_id", nullable = false)
    private UUID versionId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Kind kind;
    @Column(nullable = false)
    private String title;
    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;
    @Column(name = "pass_percent", nullable = false)
    private int passPercent;
    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "owner_scope", nullable = false, length = 12)
    private String ownerScope = "COURSE";
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;
    @Column(name = "required_for_completion", nullable = false)
    private boolean requiredForCompletion;

    public String getOwnerScope() {
        return ownerScope;
    }

    public void setOwnerScope(String scope) {
        ownerScope = scope;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID owner) {
        ownerId = owner;
    }

    public boolean isRequiredForCompletion() {
        return requiredForCompletion;
    }

    public void setRequiredForCompletion(boolean value) {
        requiredForCompletion = value;
    }

    public enum Status {
        DRAFT, PUBLISHED
    }

    public enum Kind {
        PRACTICE, MOCK, OFFICIAL
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID value) {
        id = value;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID value) {
        organizationId = value;
    }

    public UUID getVersionId() {
        return versionId;
    }

    public void setVersionId(UUID value) {
        versionId = value;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status value) {
        status = value;
    }

    public Kind getKind() {
        return kind;
    }

    public void setKind(Kind value) {
        kind = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        title = value;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int value) {
        durationSeconds = value;
    }

    public int getPassPercent() {
        return passPercent;
    }

    public void setPassPercent(int value) {
        passPercent = value;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int value) {
        maxAttempts = value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant value) {
        createdAt = value;
    }
}
