package com.skillproof.backend.organization.infrastructure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.skillproof.backend.organization.domain.Organization;

@Repository
public class OrganizationRepository {

    private final JdbcTemplate jdbc;

    public OrganizationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static UUID id(ResultSet rs, String key) throws SQLException {
        return UUID.fromString(rs.getString(key));
    }
    private static final RowMapper<Organization> MAP = (r, n) -> new Organization(id(r, "id"), id(r, "owner_user_id"), r.getString("legal_name"), r.getString("display_name"), r.getString("website"), r.getString("industry"), r.getString("country"), r.getString("registration_number"), r.getString("contact_name"), r.getString("contact_email"), r.getString("contact_phone"), Organization.Status.valueOf(r.getString("status")), r.getString("review_reason"), r.getTimestamp("created_at").toInstant(), r.getTimestamp("updated_at").toInstant());

    public Optional<Organization> find(UUID id) {
        return jdbc.query("SELECT * FROM organization WHERE id=?", MAP, id).stream().findFirst();
    }

    public Optional<Organization> owned(UUID user) {
        return jdbc.query("SELECT * FROM organization WHERE owner_user_id=?", MAP, user).stream().findFirst();
    }

    public Optional<Organization> memberOrganization(UUID user) {
        return jdbc.query("SELECT o.* FROM organization o JOIN organization_membership m ON m.organization_id=o.id WHERE m.user_id=? AND m.active=TRUE ORDER BY o.created_at LIMIT 1", MAP, user).stream().findFirst();
    }

    public List<Organization> pending() {
        return jdbc.query("SELECT * FROM organization WHERE status='PENDING' ORDER BY created_at", MAP);
    }

    public void create(Organization o) {
        jdbc.update("INSERT INTO organization(id,owner_user_id,legal_name,display_name,website,industry,country,registration_number,contact_name,contact_email,contact_phone,status,created_at,updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)", o.id(), o.ownerUserId(), o.legalName(), o.displayName(), o.website(), o.industry(), o.country(), o.registrationNumber(), o.contactName(), o.contactEmail(), o.contactPhone(), o.status().name(), Timestamp.from(o.createdAt()), Timestamp.from(o.updatedAt()));
    }

    public int resubmit(UUID id, Organization o, Instant now) {
        return jdbc.update("UPDATE organization SET legal_name=?,display_name=?,website=?,industry=?,country=?,registration_number=?,contact_name=?,contact_email=?,contact_phone=?,status='PENDING',review_reason=NULL,updated_at=? WHERE id=? AND status='REJECTED'", o.legalName(), o.displayName(), o.website(), o.industry(), o.country(), o.registrationNumber(), o.contactName(), o.contactEmail(), o.contactPhone(), Timestamp.from(now), id);
    }

    public int review(UUID id, Organization.Status decision, String reason, Instant now) {
        return jdbc.update("UPDATE organization SET status=?,review_reason=?,updated_at=? WHERE id=? AND status='PENDING'", decision.name(), reason, Timestamp.from(now), id);
    }

    public List<java.util.Map<String, Object>> reviews(UUID org) {
        return jdbc.queryForList("SELECT reviewer_user_id,decision,reason,reviewed_at FROM organization_review WHERE organization_id=? ORDER BY reviewed_at DESC", org);
    }

    public void logReview(UUID org, UUID actor, Organization.Status decision, String reason, Instant now) {
        jdbc.update("INSERT INTO organization_review(id,organization_id,reviewer_user_id,decision,reason,reviewed_at) VALUES(?,?,?,?,?,?)", UUID.randomUUID(), org, actor, decision.name(), reason, Timestamp.from(now));
    }

    public void update(UUID id, String display, String website, String industry, String phone, Instant now) {
        jdbc.update("UPDATE organization SET display_name=?,website=?,industry=?,contact_phone=?,updated_at=? WHERE id=?", display, website, industry, phone, Timestamp.from(now), id);
    }

    public void addMember(UUID org, UUID user, UUID actor, Instant now, boolean owner) {
        UUID member = UUID.randomUUID();
        jdbc.update("INSERT INTO organization_membership(id,organization_id,user_id,active,created_at) VALUES(?,?,?,TRUE,?)", member, org, user, Timestamp.from(now));
        if (owner) {
            for (String grant : List.of("MANAGE_PROFILE", "MANAGE_MEMBERS", "MANAGE_CONTENT", "ISSUE_CERTIFICATES")) {
                jdbc.update("INSERT INTO organization_authority_grant(id,membership_id,authority,active,granted_by,created_at) VALUES(?,?,?,TRUE,?,?)", UUID.randomUUID(), member, grant, actor, Timestamp.from(now));
    
            }
        }}

    public List<java.util.Map<String, Object>> members(UUID org) {
        return jdbc.queryForList("SELECT m.user_id,u.email,m.active FROM organization_membership m JOIN user_account u ON u.id=m.user_id WHERE m.organization_id=?", org);
    }

    public List<String> grants(UUID org, UUID member) {
        return jdbc.queryForList("SELECT g.authority FROM organization_authority_grant g JOIN organization_membership m ON m.id=g.membership_id WHERE m.organization_id=? AND m.user_id=? AND m.active=TRUE AND g.active=TRUE", String.class, org, member);
    }

    public boolean hasGrant(UUID org, UUID actor, String grant) {
        Integer count = jdbc.queryForObject("SELECT count(*) FROM organization_membership m JOIN organization_authority_grant g ON g.membership_id=m.id JOIN user_account u ON u.id=m.user_id JOIN organization o ON o.id=m.organization_id WHERE m.organization_id=? AND m.user_id=? AND m.active=TRUE AND g.active=TRUE AND g.authority=? AND u.status='ACTIVE' AND u.role='ORGANIZER' AND o.status='APPROVED'", Integer.class, org, actor, grant);
        return count != null && count > 0;
    }

    public void grant(UUID org, UUID member, String authority, UUID actor, boolean active, Instant now) {
        jdbc.update("INSERT INTO organization_authority_grant(id,membership_id,authority,active,granted_by,created_at) SELECT ?,m.id,?,?,?,? FROM organization_membership m WHERE m.organization_id=? AND m.user_id=? ON CONFLICT (membership_id,authority) DO UPDATE SET active=EXCLUDED.active,granted_by=EXCLUDED.granted_by,created_at=EXCLUDED.created_at", UUID.randomUUID(), authority, active, actor, Timestamp.from(now), org, member);
    }

    public int deactivate(UUID org, UUID member) {
        return jdbc.update("UPDATE organization_membership SET active=FALSE WHERE organization_id=? AND user_id=?", org, member);
    }

    public boolean activeMember(UUID org, UUID member) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM organization_membership WHERE organization_id=? AND user_id=? AND active=TRUE", Integer.class, org, member);
        return n != null && n > 0;
    }
}
