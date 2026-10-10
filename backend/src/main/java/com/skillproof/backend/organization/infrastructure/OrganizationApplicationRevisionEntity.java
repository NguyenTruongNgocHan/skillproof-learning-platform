package com.skillproof.backend.organization.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization_application_revision")
public class OrganizationApplicationRevisionEntity {

    @Id
    private UUID id;
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
    @Column(name = "revision_no", nullable = false)
    private int revisionNo;
    @Column(name = "legal_name", nullable = false)
    private String legalName;
    @Column(name = "display_name", nullable = false)
    private String displayName;
    private String website;
    @Column(nullable = false)
    private String industry;
    @Column(nullable = false)
    private String country;
    @Column(name = "registration_number")
    private String registrationNumber;
    @Column(name = "contact_name", nullable = false)
    private String contactName;
    @Column(name = "contact_email", nullable = false)
    private String contactEmail;
    @Column(name = "contact_phone")
    private String contactPhone;
    @Column(name = "document_media_ids", columnDefinition = "text")
    private String documentMediaIds;
    @Column(nullable = false)
    private String status;
    @Column(name = "reviewer_user_id")
    private UUID reviewerUserId;
    @Column(name = "review_reason")
    private String reviewReason;
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;
    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    protected OrganizationApplicationRevisionEntity() {
    }

    public OrganizationApplicationRevisionEntity(UUID id, UUID organizationId, int revisionNo,
            String legalName, String displayName, String website, String industry, String country,
            String registrationNumber, String contactName, String contactEmail, String contactPhone,
            Instant submittedAt) {
        this(id, organizationId, revisionNo, legalName, displayName, website, industry, country,
                registrationNumber, contactName, contactEmail, contactPhone, List.of(), submittedAt);
    }

    public OrganizationApplicationRevisionEntity(UUID id, UUID organizationId, int revisionNo,
            String legalName, String displayName, String website, String industry, String country,
            String registrationNumber, String contactName, String contactEmail, String contactPhone,
            List<UUID> documentMediaIds, Instant submittedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.revisionNo = revisionNo;
        this.legalName = legalName;
        this.displayName = displayName;
        this.website = website;
        this.industry = industry;
        this.country = country;
        this.registrationNumber = registrationNumber;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.documentMediaIds = documentMediaIds.stream().map(UUID::toString).collect(Collectors.joining(","));
        this.status = "PENDING";
        this.submittedAt = submittedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public int getRevisionNo() {
        return revisionNo;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getWebsite() {
        return website;
    }

    public String getIndustry() {
        return industry;
    }

    public String getCountry() {
        return country;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getStatus() {
        return status;
    }

    public UUID getReviewerUserId() {
        return reviewerUserId;
    }

    public String getReviewReason() {
        return reviewReason;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public List<UUID> getDocumentMediaIds() {
        if (documentMediaIds == null || documentMediaIds.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(documentMediaIds.split(",")).map(UUID::fromString).toList();
    }

    public void review(String decision, UUID reviewer, String reason, Instant at) {
        this.status = decision;
        this.reviewerUserId = reviewer;
        this.reviewReason = reason;
        this.reviewedAt = at;
    }
}
