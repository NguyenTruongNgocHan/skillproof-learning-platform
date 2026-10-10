package com.skillproof.backend.course.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lesson_progress")
public class LessonProgressEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;

    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    protected LessonProgressEntity() {
    }

    public LessonProgressEntity(UUID id, UUID enrollmentId, UUID lessonId, Instant completedAt) {
        this.id = id;
        this.enrollmentId = enrollmentId;
        this.lessonId = lessonId;
        this.completedAt = completedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEnrollmentId() {
        return enrollmentId;
    }

    public UUID getLessonId() {
        return lessonId;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

}
