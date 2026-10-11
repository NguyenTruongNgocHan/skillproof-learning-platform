package com.skillproof.backend.course.infrastructure;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "course_artwork")
public class CourseArtwork {
    @Id private UUID id;

    @Column(name = "logo_media_id")
    private UUID logoMediaId;

    @Column(name = "cover_media_id")
    private UUID coverMediaId;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CourseArtwork() {}

    public CourseArtwork(UUID id) {
        this.id = id;
        updatedAt = Instant.now();
    }

    public UUID getLogoMediaId() {
        return logoMediaId;
    }

    public UUID getCoverMediaId() {
        return coverMediaId;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void update(UUID logo, UUID cover) {
        logoMediaId = logo;
        coverMediaId = cover;
        updatedAt = Instant.now();
    }
}
