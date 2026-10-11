package com.skillproof.backend.feedback.application;

import com.skillproof.backend.common.exception.*;
import com.skillproof.backend.feedback.contract.*;
import com.skillproof.backend.feedback.infrastructure.*;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.operations.contract.ActivityJournal;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {
  @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
  private final Map<String, FeedbackSubjectProvider> subjects;

  @org.springframework.beans.factory.annotation.Autowired
  private ReviewModerationRepository history;

  private final LearnerReviewRepository reviews;
  private final ExperienceRepository receipts;
  private final ContentReportRepository reports;
  private final IdentityAccessQuery identity;
  private final OrganizationAuthorityQuery grants;
  private final ActivityJournal journal;

  public FeedbackService(
      List<FeedbackSubjectProvider> providers,
      LearnerReviewRepository reviews,
      ExperienceRepository receipts,
      ContentReportRepository reports,
      IdentityAccessQuery identity,
      OrganizationAuthorityQuery grants,
      ActivityJournal journal) {
    this.subjects = new HashMap<>();
    providers.forEach(p -> subjects.put(p.type(), p));
    this.reviews = reviews;
    this.receipts = receipts;
    this.reports = reports;
    this.identity = identity;
    this.grants = grants;
    this.journal = journal;
  }

  private FeedbackSubjectProvider.Subject subject(String type, UUID target, UUID actor) {
    var p = subjects.get(type);
    if (p == null) throw new BadRequestException("FEEDBACK_TYPE", "Unsupported feedback type");
    return p.subject(target, actor);
  }

  @Transactional
  public LearnerReview write(UUID actor, String type, UUID target, int rating, String body) {
    identity.lockActiveAccount(actor);
    if (!identity.isActiveLearner(actor)) throw new AccessDeniedException("Learner required");
    var s = subject(type, target, actor);
    if (actor.equals(s.authorId())
        || (s.organizationId() != null && grants.activeMember(s.organizationId(), actor)))
      throw new AccessDeniedException("Authors cannot review their own organization content");
    if (!s.experienced()) throw new AccessDeniedException("Verified learning experience required");
    if (rating < 1 || rating > 5 || body == null || body.isBlank() || body.length() > 4000)
      throw new BadRequestException("REVIEW_INVALID", "Provide a rating and review");
    var r =
        reviews
            .findByActorIdAndTypeAndTargetId(actor, type, target)
            .orElseGet(
                () ->
                    new LearnerReview(
                        UUID.randomUUID(),
                        actor,
                        s.organizationId(),
                        type,
                        target,
                        rating,
                        body,
                        "VISIBLE",
                        null,
                        null,
                        null,
                        Instant.now(),
                        Instant.now()));
    if (reviews.existsById(r.getId())) {
      r = reviews.lock(r.getId()).orElseThrow();
      entityManager.refresh(r, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
    }
    if ("HIDDEN".equals(r.getStatus()))
      throw new AccessDeniedException("Moderated review cannot be restored by its author");
    r.edit(rating, body);
    reviews.save(r);
    if (s.authorId() != null)
      journal.notify(
          s.authorId(),
          s.organizationId(),
          "FEEDBACK",
          "New or updated feedback: " + s.label(),
          "/organizer/feedback");
    return r;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> publicList(String type, UUID target, int page) {
    if (!Set.of("COURSE", "RESOURCE", "CERTIFICATE").contains(type))
      throw new AccessDeniedException("Assessment feedback is private");
    subject(type, target, null);
    var rows =
        reviews.findByTypeAndTargetIdAndStatusOrderByCreatedAtDesc(
            type, target, "VISIBLE", PageRequest.of(Math.max(0, page), 20));
    var summary = reviews.summary(type, target);
    Object[] v = summary.get(0);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("average", v[0]);
    result.put("count", v[1]);
    result.put(
        "reviews",
        rows.map(
            r ->
                Map.of(
                    "id",
                    r.getId(),
                    "rating",
                    r.getRating(),
                    "body",
                    r.getBody(),
                    "reply",
                    r.getReply() == null ? "" : r.getReply(),
                    "createdAt",
                    r.getCreatedAt(),
                    "verifiedExperience",
                    true)));
    return result;
  }

  public Page<LearnerReview> organization(UUID actor, UUID org, int page) {
    if (!grants.hasAuthority(org, actor, "MANAGE_FEEDBACK"))
      throw new AccessDeniedException("Feedback authority required");
    return reviews.findByOrganizationIdOrderByCreatedAtDesc(
        org, PageRequest.of(Math.max(0, page), 25));
  }

  @Transactional
  public LearnerReview reply(UUID actor, UUID id, String body) {
    var r = reviews.lock(id).orElseThrow();
    if (r.getOrganizationId() == null
        || !grants.hasAuthority(r.getOrganizationId(), actor, "MANAGE_FEEDBACK"))
      throw new AccessDeniedException("Feedback authority required");
    r.respond(actor, body);
    journal.record(actor, r.getOrganizationId(), "REVIEW_REPLIED", id, body);
    return r;
  }

  @Transactional
  public ContentReport report(UUID actor, UUID id, String reason) {
    identity.lockActiveAccount(actor);
    reviews.findById(id).orElseThrow();
    if (reports.existsByActorIdAndReviewId(actor, id))
      throw new ConflictException("REPORT_EXISTS", "Report already submitted");
    return reports.save(
        new ContentReport(UUID.randomUUID(), actor, id, reason, "OPEN", null, null, Instant.now()));
  }

  public LearnerReview adminReview(UUID actor, UUID id) {
    if (!identity.isActiveAdmin(actor)) throw new AccessDeniedException("Admin required");
    return reviews
        .findById(id)
        .orElseThrow(() -> new NotFoundException("REVIEW_NOT_FOUND", "Review not found"));
  }

  public Page<ContentReport> reports(UUID actor, int page) {
    if (!identity.isActiveAdmin(actor)) throw new AccessDeniedException("Admin required");
    return reports.findByStatusOrderByCreatedAtAsc("OPEN", PageRequest.of(Math.max(0, page), 25));
  }

  @Transactional
  public void resolve(UUID actor, UUID id, boolean hide, String reason) {
    if (!identity.isActiveAdmin(actor)) throw new AccessDeniedException("Admin required");
    if (reason == null || reason.isBlank() || reason.length() > 1000)
      throw new BadRequestException("MODERATION_REASON", "Provide a reason up to 1,000 characters");
    var report = reports.lock(id).orElseThrow();
    if (!"OPEN".equals(report.getStatus()))
      throw new ConflictException("REPORT_RESOLVED", "Already resolved");
    var review = reviews.lock(report.getReviewId()).orElseThrow();
    if (hide) review.moderate("HIDDEN", reason);
    report.resolve(actor, reason);
    history.save(new ReviewModerationEntry(review.getId(), id, actor, hide, reason));
    journal.record(actor, review.getOrganizationId(), "REPORT_RESOLVED", id, reason);
  }

  @Transactional(readOnly = true)
  public java.util.List<ReviewModerationEntry> moderationHistory(UUID actor, UUID id) {
    var review =
        reviews
            .findById(id)
            .orElseThrow(() -> new NotFoundException("REVIEW_NOT_FOUND", "Review not found"));
    if (!identity.isActiveAdmin(actor)
        && (review.getOrganizationId() == null
            || !grants.hasAuthority(review.getOrganizationId(), actor, "MANAGE_FEEDBACK")))
      throw new AccessDeniedException("Moderation history authority required");
    return history.findByReviewIdOrderByCreatedAtDesc(id);
  }
}
