package com.skillproof.backend.operations.infrastructure;

import jakarta.persistence.*;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "workspace_notice")
public class WorkspaceNotice {
    @Id private UUID id;

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "category", nullable = false, length = 80)
    private String category;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "link", nullable = false, length = 500)
    private String link;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    protected WorkspaceNotice() {}

    public WorkspaceNotice(
            UUID id,
            UUID recipientId,
            UUID organizationId,
            String category,
            String message,
            String link,
            Instant createdAt,
            Instant readAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.organizationId = organizationId;
        this.category = category;
        this.message = message;
        this.link = link;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public String getLink() {
        return link;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void markRead() {
        if (readAt == null) readAt = Instant.now();
    }
}
