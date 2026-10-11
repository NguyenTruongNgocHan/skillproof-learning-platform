package com.skillproof.backend.pricing.infrastructure;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "course_promotion")
public class CoursePromotion {
    @Id private UUID id;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "name", nullable = false, length = 180)
    private String name;

    @Column(name = "percent", nullable = false)
    private int percent;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    protected CoursePromotion() {}

    public CoursePromotion(
            UUID id,
            UUID courseId,
            String name,
            int percent,
            Instant startsAt,
            Instant endsAt,
            boolean active,
            UUID createdBy) {
        this.id = id;
        this.courseId = courseId;
        this.name = name;
        this.percent = percent;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.active = active;
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public String getName() {
        return name;
    }

    public int getPercent() {
        return percent;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public boolean getActive() {
        return active;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void cancel() {
        active = false;
    }
}
