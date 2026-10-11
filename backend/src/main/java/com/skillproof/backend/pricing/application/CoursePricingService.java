package com.skillproof.backend.pricing.application;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.course.contract.CourseWorkspaceQuery;
import com.skillproof.backend.operations.contract.ActivityJournal;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import com.skillproof.backend.pricing.contract.CourseSaleQuery;
import com.skillproof.backend.pricing.infrastructure.*;
import java.time.*;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoursePricingService implements CourseSaleQuery {
  private final CourseCommercialRepository commercial;
  private final PriceChangeRepository changes;

  @org.springframework.beans.factory.annotation.Autowired
  private com.skillproof.backend.organization.contract.OrganizationNotificationRecipients
      recipients;

  private final PromotionRepository promotions;
  private final CourseWorkspaceQuery courses;
  private final OrganizationAuthorityQuery grants;
  private final ActivityJournal journal;

  public CoursePricingService(
      CourseCommercialRepository commercial,
      PriceChangeRepository changes,
      PromotionRepository promotions,
      CourseWorkspaceQuery courses,
      OrganizationAuthorityQuery grants,
      ActivityJournal journal) {
    this.commercial = commercial;
    this.changes = changes;
    this.promotions = promotions;
    this.courses = courses;
    this.grants = grants;
    this.journal = journal;
  }

  private CourseWorkspaceQuery.Context require(UUID actor, UUID version, String authority) {
    var c = courses.context(version);
    if (c.organizationId() == null || !grants.hasAuthority(c.organizationId(), actor, authority))
      throw new AccessDeniedException(authority + " required");
    return c;
  }

  private void price(long p) {
    if (p != 0 && (p < 5000 || p > 1000000000))
      throw new BadRequestException("PRICE_INVALID", "Price must be 0 or 5000..1000000000 VND");
  }

  private CourseCommercial lock(CourseWorkspaceQuery.Context c) {
    courses.lockCourse(c.courseId());
    return commercial
        .lock(c.courseId())
        .orElseGet(
            () ->
                commercial.saveAndFlush(
                    new CourseCommercial(
                        c.courseId(),
                        c.organizationId(),
                        "ACTIVE",
                        c.accessMode(),
                        c.price(),
                        c.certificationPrice(),
                        Instant.now())));
  }

  @Transactional
  public CourseCommercial configure(
      UUID actor, UUID version, String mode, long learning, long certification, String reason) {
    var c = require(actor, version, "MANAGE_PRICING");
    price(learning);
    price(certification);
    if (!List.of("PUBLIC", "RESTRICTED").contains(mode)
        || ("RESTRICTED".equals(mode) && (learning != 0 || certification != 0)))
      throw new BadRequestException("OFFER_INVALID", "Invalid offer");
    var p = lock(c);
    changes.save(
        new PriceChange(
            UUID.randomUUID(),
            c.courseId(),
            actor,
            mode,
            p.getPriceVnd(),
            learning,
            p.getCertificationPriceVnd(),
            certification,
            reason,
            Instant.now()));
    p.configure(mode, learning, certification);
    journal.record(actor, c.organizationId(), "PRICE_CHANGED", c.courseId(), reason);
    return p;
  }

  @Transactional
  public CourseCommercial lifecycle(UUID actor, UUID version, String state) {
    var c = require(actor, version, "MANAGE_CONTENT");
    if (!List.of("ACTIVE", "CLOSED", "ARCHIVED").contains(state))
      throw new BadRequestException("LIFECYCLE_INVALID", "Invalid lifecycle");
    var p = lock(c);
    p.lifecycle(state);
    journal.record(actor, c.organizationId(), "COURSE_" + state, c.courseId(), state);
    return p;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> detail(UUID actor, UUID version) {
    var c = courses.context(version);
    if (c.organizationId() == null
        || (!grants.hasAuthority(c.organizationId(), actor, "MANAGE_PRICING")
            && !grants.hasAuthority(c.organizationId(), actor, "MANAGE_CONTENT")))
      throw new AccessDeniedException("Pricing or content authority required");
    var base = commercial.findById(c.courseId());
    return Map.of(
        "baseOffer",
        new Sale(
            base.map(CourseCommercial::getState).orElse("ACTIVE"),
            base.map(CourseCommercial::getAccessMode).orElse(c.accessMode()),
            base.map(CourseCommercial::getPriceVnd).orElse(c.price()),
            base.map(CourseCommercial::getCertificationPriceVnd).orElse(c.certificationPrice())),
        "offer",
        sale(version, c.accessMode(), c.price(), c.certificationPrice()),
        "history",
        changes.findByCourseIdOrderByCreatedAtDesc(c.courseId()),
        "promotions",
        promotions.findByCourseIdOrderByStartsAtDesc(c.courseId()));
  }

  @Transactional
  public CoursePromotion promote(
      UUID actor, UUID version, String name, int percent, Instant start, Instant end) {
    var c = require(actor, version, "MANAGE_PRICING");
    lock(c);
    if (percent < 1
        || percent > 90
        || start == null
        || end == null
        || !end.isAfter(start)
        || !end.isAfter(Instant.now()))
      throw new BadRequestException("PROMOTION_INVALID", "Invalid promotion window");
    if (promotions.findByCourseIdOrderByStartsAtDesc(c.courseId()).stream()
        .anyMatch(
            p -> p.getActive() && p.getStartsAt().isBefore(end) && start.isBefore(p.getEndsAt())))
      throw new ConflictException("PROMOTION_OVERLAP", "Cancel overlapping promotion first");
    var p =
        promotions.save(
            new CoursePromotion(
                UUID.randomUUID(), c.courseId(), name, percent, start, end, true, actor));
    journal.record(actor, c.organizationId(), "PROMOTION_CREATED", p.getId(), name);
    for (UUID recipient : recipients.forAuthority(c.organizationId(), "MANAGE_PRICING"))
      journal.notify(
          recipient,
          c.organizationId(),
          "PROMOTION",
          "Promotion scheduled: " + name,
          "/organizer/courses/" + c.courseId());
    return p;
  }

  @Transactional
  public void cancel(UUID actor, UUID version, UUID id) {
    var c = require(actor, version, "MANAGE_PRICING");
    lock(c);
    var p = promotions.findById(id).orElseThrow();
    if (!p.getCourseId().equals(c.courseId())) throw new AccessDeniedException("Course mismatch");
    p.cancel();
    journal.record(actor, c.organizationId(), "PROMOTION_CANCELLED", id, p.getName());
    for (UUID recipient : recipients.forAuthority(c.organizationId(), "MANAGE_PRICING"))
      journal.notify(
          recipient,
          c.organizationId(),
          "PROMOTION",
          "Promotion cancelled: " + p.getName(),
          "/organizer/courses/" + c.courseId());
  }

  @Transactional(readOnly = true)
  public Sale sale(UUID version, String mode, long learning, long certification) {
    var c = courses.context(version);
    var p = commercial.findById(c.courseId());
    String state = p.map(CourseCommercial::getState).orElse("ACTIVE");
    String access = p.map(CourseCommercial::getAccessMode).orElse(mode);
    long base = p.map(CourseCommercial::getPriceVnd).orElse(learning),
        cert = p.map(CourseCommercial::getCertificationPriceVnd).orElse(certification);
    Instant now = Instant.now();
    int percent =
        promotions.findByCourseIdOrderByStartsAtDesc(c.courseId()).stream()
            .filter(
                x -> x.getActive() && !now.isBefore(x.getStartsAt()) && now.isBefore(x.getEndsAt()))
            .mapToInt(CoursePromotion::getPercent)
            .findFirst()
            .orElse(0);
    return new Sale(state, access, discount(base, percent), discount(cert, percent));
  }

  private long discount(long price, int percent) {
    return price == 0 ? 0 : Math.max(5000, price * (100 - percent) / 100);
  }
}
