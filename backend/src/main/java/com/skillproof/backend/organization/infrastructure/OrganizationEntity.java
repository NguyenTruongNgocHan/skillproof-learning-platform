package com.skillproof.backend.organization.infrastructure;

import java.time.Instant;
import java.util.UUID;

import com.skillproof.backend.organization.domain.Organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization")
public class OrganizationEntity {

    @Id
    private UUID id;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(length = 500)
    private String website;

    @Column(length = 120)
    private String industry;

    @Column(length = 120)
    private String country;

    @Column(name = "registration_number", length = 120)
    private String registrationNumber;

    @Column(name = "contact_name", length = 150)
    private String contactName;

    @Column(name = "contact_email", length = 320)
    private String contactEmail;

    @Column(name = "contact_phone", length = 60)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Organization.Status status;

    @Column(name = "review_reason", length = 1000)
    private String reviewReason;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    protected OrganizationEntity() {
    }

    OrganizationEntity(Organization organization) {
        this.id = organization.id();
        this.ownerUserId = organization.ownerUserId();
        this.legalName = organization.legalName();
        this.displayName = organization.displayName();
        this.website = organization.website();
        this.industry = organization.industry();
        this.country = organization.country();
        this.registrationNumber = organization.registrationNumber();
        this.contactName = organization.contactName();
        this.contactEmail = organization.contactEmail();
        this.contactPhone = organization.contactPhone();
        this.status = organization.status();
        this.reviewReason = organization.reviewReason();
        this.createdAt = organization.createdAt();
        this.updatedAt = organization.updatedAt();
    }

    Organization toDomain() {
        return new Organization(
                id,
                ownerUserId,
                legalName,
                displayName,
                website,
                industry,
                country,
                registrationNumber,
                contactName,
                contactEmail,
                contactPhone,
                status,
                reviewReason,
                createdAt,
                updatedAt
        );
    }

    void resubmit(Organization replacement, Instant now) {
        this.legalName = replacement.legalName();
        this.displayName = replacement.displayName();
        this.website = replacement.website();
        this.industry = replacement.industry();
        this.country = replacement.country();
        this.registrationNumber = replacement.registrationNumber();
        this.contactName = replacement.contactName();
        this.contactEmail = replacement.contactEmail();
        this.contactPhone = replacement.contactPhone();
        this.status = Organization.Status.DRAFT;
        this.reviewReason = null;
        this.updatedAt = now;
    }

    void saveDraft(Organization replacement, Instant now) {
        this.legalName = replacement.legalName();
        this.displayName = replacement.displayName();
        this.website = replacement.website();
        this.industry = replacement.industry();
        this.country = replacement.country();
        this.registrationNumber = replacement.registrationNumber();
        this.contactName = replacement.contactName();
        this.contactEmail = replacement.contactEmail();
        this.contactPhone = replacement.contactPhone();
        this.status = Organization.Status.DRAFT;
        this.reviewReason = null;
        this.updatedAt = now;
    }

    void review(Organization.Status decision, String reason, Instant now) {
        this.status = decision;
        this.reviewReason = reason;
        this.updatedAt = now;
    }

    void updateProfile(
            String display,
            String website,
            String industry,
            String phone,
            Instant now
    ) {
        this.displayName = display;
        this.website = website;
        this.industry = industry;
        this.contactPhone = phone;
        this.updatedAt = now;
    }
}
