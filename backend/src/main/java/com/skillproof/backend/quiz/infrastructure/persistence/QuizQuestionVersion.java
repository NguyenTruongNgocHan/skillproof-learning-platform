package com.skillproof.backend.quiz.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "question_version")
public class QuizQuestionVersion {

    @Id
    private UUID id;
    @Column(name = "question_id", nullable = false)
    private UUID questionId;
    @Column(name = "version_no", nullable = false)
    private int versionNo;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private String stem;

    public UUID getId() {
        return id;
    }

    public void setId(UUID value) {
        id = value;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID value) {
        questionId = value;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(int value) {
        versionNo = value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant value) {
        createdAt = value;
    }

    public String getStem() {
        return stem;
    }

    public void setStem(String value) {
        stem = value;
    }
}
