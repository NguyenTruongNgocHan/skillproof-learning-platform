package com.skillproof.backend.organization.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrganizationReviewChecklistRepository {
  public record Item(String key, boolean checked, String note) {}

  private final JdbcTemplate jdbc;

  public OrganizationReviewChecklistRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public boolean revisionExists(UUID revisionId) {
    Integer count =
        jdbc.queryForObject(
            "select count(*) from organization_application_revision where id=?",
            Integer.class,
            revisionId);
    return count != null && count > 0;
  }

  public List<Item> load(UUID revisionId, UUID reviewerId) {
    return jdbc.query(
        "select criterion_key,checked,note from organization_revision_review_check where"
            + " revision_id=? and reviewer_user_id=? order by criterion_key",
        (rs, n) -> new Item(rs.getString(1), rs.getBoolean(2), rs.getString(3)),
        revisionId,
        reviewerId);
  }

  public Instant updatedAt(UUID revisionId, UUID reviewerId) {
    return jdbc.query(
        "select max(updated_at) from organization_revision_review_check where revision_id=? and"
            + " reviewer_user_id=?",
        rs -> rs.next() && rs.getTimestamp(1) != null ? rs.getTimestamp(1).toInstant() : null,
        revisionId,
        reviewerId);
  }

  public void save(UUID revisionId, UUID reviewerId, Item item) {
    jdbc.update(
        "insert into"
            + " organization_revision_review_check(revision_id,reviewer_user_id,criterion_key,checked,note,updated_at)"
            + " values (?,?,?,?,?,now()) on conflict (revision_id,reviewer_user_id,criterion_key)"
            + " do update set checked=excluded.checked,note=excluded.note,updated_at=now()",
        revisionId,
        reviewerId,
        item.key(),
        item.checked(),
        item.note());
    jdbc.update(
        "insert into"
            + " organization_revision_review_check_event(revision_id,reviewer_user_id,criterion_key,checked,note,created_at)"
            + " values (?,?,?,?,?,now())",
        revisionId,
        reviewerId,
        item.key(),
        item.checked(),
        item.note());
  }

  public long overdueCount() {
    Long n =
        jdbc.queryForObject(
            "select count(*) from organization_application_revision where status='PENDING' and"
                + " submitted_at < now() - interval '72 hours'",
            Long.class);
    return n == null ? 0 : n;
  }

  public java.util.List<DailyReviewVolume> dailyReviewVolume() {
    return jdbc.query(
        """
        select cast(d.day as date),
          (select count(*) from organization_application_revision r where cast(r.submitted_at as date) = cast(d.day as date)) as submitted,
          (select count(*) from organization_application_revision r where r.reviewed_at is not null and cast(r.reviewed_at as date) = cast(d.day as date)) as reviewed
        from generate_series(current_date - interval '6 days', current_date, interval '1 day') d(day)
        order by d.day
        """,
        (rs, n) ->
            new DailyReviewVolume(rs.getDate(1).toLocalDate(), rs.getLong(2), rs.getLong(3)));
  }

  public record DailyReviewVolume(java.time.LocalDate day, long submitted, long reviewed) {}

  public java.util.List<AgeBucket> pendingAgeBuckets() {
    return jdbc.query(
        """
        select case when submitted_at >= now() - interval '24 hours' then '0–24h'
                    when submitted_at >= now() - interval '48 hours' then '24–48h'
                    when submitted_at >= now() - interval '72 hours' then '48–72h'
                    else '>72h' end as age, count(*)
        from organization_application_revision where status='PENDING'
        group by age
        """,
        (rs, n) -> new AgeBucket(rs.getString(1), rs.getLong(2)));
  }

  public record AgeBucket(String age, long count) {}
}
