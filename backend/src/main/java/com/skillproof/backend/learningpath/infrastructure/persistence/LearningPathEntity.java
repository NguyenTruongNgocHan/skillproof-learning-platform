package com.skillproof.backend.learningpath.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_path")
public class LearningPathEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "learner_id", nullable = false)
    private UUID learnerId;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "goal", nullable = false, length = 1000)
    private String goal;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected LearningPathEntity() {
    }

    public LearningPathEntity(UUID id, UUID learnerId, String title, String goal, Instant createdAt) {
        this.id = id;
        this.learnerId = learnerId;
        this.title = title;
        this.goal = goal;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getLearnerId() {
        return learnerId;
    }

    public String getTitle() {
        return title;
    }

    public String getGoal() {
        return goal;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}
