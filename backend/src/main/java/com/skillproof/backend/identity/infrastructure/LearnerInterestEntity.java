package com.skillproof.backend.identity.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "learner_interest")
public class LearnerInterestEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "source", nullable = false, length = 30)
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LearnerInterestEntity() {
    }

    public LearnerInterestEntity(
            UUID id,
            UUID userId,
            String label,
            String source,
            Instant createdAt) {

        this.id = id;
        this.userId = userId;
        this.label = label;
        this.source = source;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getLabel() {
        return label;
    }

    public String getSource() {
        return source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
