package com.skillproof.backend.quiz.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "question_bank")
public class QuizBank {

    @Id
    private UUID id;
    @Column(name = "organization_id")
    private UUID organizationId;
    @Column(nullable = false)
    private String title;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        title = value;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID value) {
        createdBy = value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant value) {
        createdAt = value;
    }
}
