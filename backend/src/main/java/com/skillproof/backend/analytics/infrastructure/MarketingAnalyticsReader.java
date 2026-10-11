package com.skillproof.backend.analytics.infrastructure;

import jakarta.persistence.*;

import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public class MarketingAnalyticsReader {
    @PersistenceContext private EntityManager em;
    private static final String WHERE =
            " organization_id=:organization AND created_at>=:start AND created_at<:end AND"
                    + " (:version='' OR cast(version_id as text)=:version) AND (:campaign='' OR"
                    + " campaign=:campaign) ";

    @SuppressWarnings("unchecked")
    private List<Object[]> rows(
            String select,
            UUID organization,
            Instant start,
            Instant end,
            String version,
            String campaign) {
        return em.createNativeQuery(select)
                .setParameter("organization", organization)
                .setParameter("start", start)
                .setParameter("end", end)
                .setParameter("version", version)
                .setParameter("campaign", campaign)
                .getResultList();
    }

    public List<Object[]> metrics(UUID o, Instant s, Instant e, String v, String c) {
        return rows(
                "SELECT kind,count(*),count(DISTINCT visitor_id),count(DISTINCT actor_id) FROM"
                        + " marketing_event WHERE"
                        + WHERE
                        + " GROUP BY kind ORDER BY kind",
                o,
                s,
                e,
                v,
                c);
    }

    public List<Object[]> daily(UUID o, Instant s, Instant e, String v, String c) {
        return rows(
                "SELECT (created_at AT TIME ZONE 'UTC')::date,kind,count(*) FROM marketing_event"
                        + " WHERE"
                        + WHERE
                        + " GROUP BY (created_at AT TIME ZONE 'UTC')::date,kind ORDER BY"
                        + " (created_at AT TIME ZONE 'UTC')::date,kind",
                o,
                s,
                e,
                v,
                c);
    }

    public List<Object[]> courses(UUID o, Instant s, Instant e, String v, String c) {
        return rows(
                "SELECT version_id,kind,count(*) FROM marketing_event WHERE"
                        + WHERE
                        + " GROUP BY version_id,kind ORDER BY version_id,kind",
                o,
                s,
                e,
                v,
                c);
    }

    public List<Object[]> sources(UUID o, Instant s, Instant e, String v, String c) {
        return rows(
                "SELECT source,campaign,kind,count(*) FROM marketing_event WHERE"
                        + WHERE
                        + " GROUP BY source,campaign,kind ORDER BY source,campaign,kind",
                o,
                s,
                e,
                v,
                c);
    }

    public Map<String, Object> funnel(UUID o, Instant s, Instant e, String v, String c) {
        String sql =
                "WITH cohort AS (SELECT actor_id,version_id,min(created_at) AS viewed FROM"
                        + " marketing_event WHERE"
                        + WHERE
                        + " AND actor_id IS NOT NULL AND version_id IS NOT NULL AND"
                        + " kind='DETAIL_VIEW' GROUP BY actor_id,version_id), stages AS (SELECT"
                        + " c.actor_id,c.version_id,c.viewed,min(e.created_at) FILTER(WHERE"
                        + " e.kind='CHECKOUT') AS checkout,min(e.created_at) FILTER(WHERE"
                        + " e.kind='ENROLLMENT') AS enrolled FROM cohort c LEFT JOIN"
                        + " marketing_event e ON e.organization_id=:organization AND"
                        + " e.actor_id=c.actor_id AND e.version_id=c.version_id AND"
                        + " e.created_at>=c.viewed AND e.created_at<:end GROUP BY"
                        + " c.actor_id,c.version_id,c.viewed), learned AS (SELECT"
                        + " s.*,min(e.created_at) FILTER(WHERE e.kind='LEARNING') AS"
                        + " learned,min(e.created_at) FILTER(WHERE e.kind='COMPLETION') AS"
                        + " completed FROM stages s LEFT JOIN marketing_event e ON"
                        + " e.organization_id=:organization AND e.actor_id=s.actor_id AND"
                        + " e.version_id=s.version_id AND e.created_at>=s.enrolled AND"
                        + " e.created_at<:end GROUP BY"
                        + " s.actor_id,s.version_id,s.viewed,s.checkout,s.enrolled) SELECT"
                        + " count(*),count(*) FILTER(WHERE checkout IS NOT NULL),count(*)"
                        + " FILTER(WHERE enrolled IS NOT NULL),count(*) FILTER(WHERE learned IS NOT"
                        + " NULL),count(*) FILTER(WHERE completed IS NOT NULL AND learned IS NOT"
                        + " NULL AND completed>=learned) FROM learned";
        var row = rows(sql, o, s, e, v, c).get(0);
        return Map.of(
                "detailViews",
                row[0],
                "checkouts",
                row[1],
                "enrollments",
                row[2],
                "learning",
                row[3],
                "completions",
                row[4],
                "basis",
                "Authenticated learner-course cohorts; anonymous reach is reported separately;"
                        + " checkout is optional for free/granted access");
    }
}
