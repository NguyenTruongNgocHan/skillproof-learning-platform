package com.skillproof.backend.pricing.infrastructure;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "course_commercial")
public class CourseCommercial {
    @Id private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "state", nullable = false, length = 16)
    private String state;

    @Column(name = "access_mode", nullable = false, length = 16)
    private String accessMode;

    @Column(name = "price_vnd", nullable = false)
    private long priceVnd;

    @Column(name = "certification_price_vnd", nullable = false)
    private long certificationPriceVnd;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CourseCommercial() {}

    public CourseCommercial(
            UUID id,
            UUID organizationId,
            String state,
            String accessMode,
            long priceVnd,
            long certificationPriceVnd,
            Instant updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.state = state;
        this.accessMode = accessMode;
        this.priceVnd = priceVnd;
        this.certificationPriceVnd = certificationPriceVnd;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getState() {
        return state;
    }

    public String getAccessMode() {
        return accessMode;
    }

    public long getPriceVnd() {
        return priceVnd;
    }

    public long getCertificationPriceVnd() {
        return certificationPriceVnd;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void configure(String mode, long learning, long certification) {
        accessMode = mode;
        priceVnd = learning;
        certificationPriceVnd = certification;
        updatedAt = Instant.now();
    }

    public void lifecycle(String value) {
        state = value;
        updatedAt = Instant.now();
    }
}
