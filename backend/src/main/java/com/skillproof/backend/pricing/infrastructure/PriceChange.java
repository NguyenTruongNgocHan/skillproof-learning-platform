package com.skillproof.backend.pricing.infrastructure;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "course_price_change")
public class PriceChange {
    @Id private UUID id;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "access_mode", nullable = false, length = 16)
    private String accessMode;

    @Column(name = "previous_price", nullable = false)
    private long previousPrice;

    @Column(name = "price_vnd", nullable = false)
    private long priceVnd;

    @Column(name = "previous_certification_price", nullable = false)
    private long previousCertificationPrice;

    @Column(name = "certification_price_vnd", nullable = false)
    private long certificationPriceVnd;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PriceChange() {}

    public PriceChange(
            UUID id,
            UUID courseId,
            UUID actorId,
            String accessMode,
            long previousPrice,
            long priceVnd,
            long previousCertificationPrice,
            long certificationPriceVnd,
            String reason,
            Instant createdAt) {
        this.id = id;
        this.courseId = courseId;
        this.actorId = actorId;
        this.accessMode = accessMode;
        this.previousPrice = previousPrice;
        this.priceVnd = priceVnd;
        this.previousCertificationPrice = previousCertificationPrice;
        this.certificationPriceVnd = certificationPriceVnd;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getAccessMode() {
        return accessMode;
    }

    public long getPreviousPrice() {
        return previousPrice;
    }

    public long getPriceVnd() {
        return priceVnd;
    }

    public long getPreviousCertificationPrice() {
        return previousCertificationPrice;
    }

    public long getCertificationPriceVnd() {
        return certificationPriceVnd;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
