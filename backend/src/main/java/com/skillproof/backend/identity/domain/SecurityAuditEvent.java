package com.skillproof.backend.identity.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_audit_event")
public class SecurityAuditEvent {

    @Id
    private UUID id;
    private UUID userAccountId;
    private String eventType;
    private String outcome;
    private String subject;
    private String ipAddress;
    private String userAgent;
    private String details;
    private Instant occurredAt;

    protected SecurityAuditEvent() {
    }

    public static SecurityAuditEvent record(UUID userId, String type, String outcome, String subject,
            String ip, String agent, String details, Instant now) {
        var event = new SecurityAuditEvent();
        event.id = UUID.randomUUID();
        event.userAccountId = userId;
        event.eventType = type;
        event.outcome = outcome;
        event.subject = trim(subject, 320);
        event.ipAddress = trim(ip, 64);
        event.userAgent = trim(agent, 500);
        event.details = trim(details, 1000);
        event.occurredAt = now;
        return event;
    }

    private static String trim(String value, int max) {
        return value == null ? null : value.substring(0, Math.min(value.length(), max));
    }
}
