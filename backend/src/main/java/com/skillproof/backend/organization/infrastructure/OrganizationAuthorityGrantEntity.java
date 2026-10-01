package com.skillproof.backend.organization.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization_authority_grant")
public class OrganizationAuthorityGrantEntity {

    @Id
    private UUID id;

    @Column(name = "membership_id", nullable = false)
    private UUID membershipId;

    @Column(nullable = false, length = 40)
    private String authority;

    private boolean active;

    @Column(name = "granted_by", nullable = false)
    private UUID grantedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OrganizationAuthorityGrantEntity() {
    }

    OrganizationAuthorityGrantEntity(UUID membershipId, String authority,
            boolean active, UUID grantedBy, Instant createdAt) {
        this.id = UUID.randomUUID();
        this.membershipId = membershipId;
        this.authority = authority;
        update(active, grantedBy, createdAt);
    }

    void update(boolean active, UUID grantedBy, Instant createdAt) {
        this.active = active;
        this.grantedBy = grantedBy;
        this.createdAt = createdAt;
    }

    public String getAuthority() {
        return authority;
    }
}
