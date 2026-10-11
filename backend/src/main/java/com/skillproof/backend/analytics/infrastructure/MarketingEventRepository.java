package com.skillproof.backend.analytics.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.time.*;
import java.util.*;

public interface MarketingEventRepository extends JpaRepository<MarketingEvent, UUID> {
    @Query(
            "select e.kind, count(e), count(distinct e.visitorId) from MarketingEvent e where"
                    + " e.organizationId=:org and e.createdAt>=:start and e.createdAt<:end group by"
                    + " e.kind")
    List<Object[]> metrics(UUID org, Instant start, Instant end);

    @Query(
            "select e.versionId, e.kind, count(e) from MarketingEvent e where e.organizationId=:org"
                    + " and e.createdAt>=:start and e.createdAt<:end group by e.versionId,e.kind")
    List<Object[]> byCourse(UUID org, Instant start, Instant end);

    @Query(
            "select e.source,e.campaign,e.kind,count(e) from MarketingEvent e where"
                    + " e.organizationId=:org and e.createdAt>=:start and e.createdAt<:end group by"
                    + " e.source,e.campaign,e.kind")
    List<Object[]> sources(UUID org, Instant start, Instant end);

    @Query(
            value =
                    "select (created_at AT TIME ZONE 'UTC')::date,kind,count(*) from"
                        + " marketing_event where organization_id=:org and created_at>=:start and"
                        + " created_at<:end group by (created_at AT TIME ZONE 'UTC')::date,kind"
                        + " order by (created_at AT TIME ZONE 'UTC')::date",
            nativeQuery = true)
    List<Object[]> daily(UUID org, Instant start, Instant end);

    java.util.Optional<MarketingEvent> findFirstByActorIdAndVersionIdAndKindOrderByCreatedAtDesc(
            UUID actor, UUID version, String kind);
}
