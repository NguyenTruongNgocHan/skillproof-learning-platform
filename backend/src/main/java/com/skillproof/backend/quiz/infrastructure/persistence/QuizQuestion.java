package com.skillproof.backend.quiz.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "question")
public class QuizQuestion {

    @Id
    private UUID id;
    @Column(name = "bank_id", nullable = false)
    private UUID bankId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID value) {
        id = value;
    }

    public UUID getBankId() {
        return bankId;
    }

    public void setBankId(UUID value) {
        bankId = value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant value) {
        createdAt = value;
    }
}
