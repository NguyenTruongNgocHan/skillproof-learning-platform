package com.skillproof.backend.freeze;

import static org.assertj.core.api.Assertions.*;

import com.skillproof.backend.access.application.*;
import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.course.application.*;
import com.skillproof.backend.course.infrastructure.*;
import com.skillproof.backend.identity.IdentityTestMailConfig;
import com.skillproof.backend.identity.domain.*;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import com.skillproof.backend.media.application.MediaService;
import com.skillproof.backend.organization.application.*;
import com.skillproof.backend.organization.domain.Organization;
import com.skillproof.backend.quiz.application.*;
import com.skillproof.backend.quiz.infrastructure.persistence.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exercises actual owner-module services and persistence, rather than mocking the business rules.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(IdentityTestMailConfig.class)
@Transactional
class FreezeBaselineIntegrationTest {
  @Autowired UserAccountRepository accounts;
  @Autowired OrganizationService organizations;
  @Autowired com.skillproof.backend.organization.infrastructure.OrganizationRepository membership;
  @Autowired OrganizationEvidenceService evidence;
  @Autowired MediaService media;
  @Autowired CourseAuthoringService courses;
  @Autowired CourseContentService content;
  @Autowired LessonService lessons;
  @Autowired CourseContextRepository versions;
  @Autowired CourseEnrollmentService enrollments;
  @Autowired CourseProgressService progress;
  @Autowired LessonProgressService lessonProgress;
  @Autowired CourseReadinessService readiness;
  @Autowired CourseMetadataService metadata;
  @Autowired LearnerHistoryService history;
  @Autowired com.skillproof.backend.analytics.infrastructure.LearningSignalRepository signals;
  @Autowired CoursePreviewService previews;
  @Autowired CourseModerationService moderation;
  @Autowired QuizAuthoringService questions;
  @Autowired AssessmentAuthoringService assessments;
  @Autowired QuizAttemptService attempts;
  @Autowired QuizAttemptRepository attemptRepository;
  @Autowired AccessGrantService grants;
  @Autowired AccessEntitlementService entitlements;
  @jakarta.persistence.PersistenceContext jakarta.persistence.EntityManager entityManager;

  UUID account(UserRole role) {
    var email = UUID.randomUUID() + "@example.org";
    var user =
        role == UserRole.ADMIN
            ? UserAccount.newAdmin(email, "test-hash", "Admin", Instant.now())
            : UserAccount.newAccount(email, "test-hash", "User", role);
    if (role != UserRole.ADMIN) user.verifyEmail(Instant.now());
    return accounts.saveAndFlush(user).getId();
  }

  record Org(UUID owner, UUID admin, UUID id) {}

  Org organization(boolean approve, boolean duplicateImages) {
    UUID owner = account(UserRole.ORGANIZER), admin = account(UserRole.ADMIN);
    var org =
        organizations.create(
            owner,
            new OrganizationService.Create(
                "Legal Ltd",
                "Training",
                null,
                "Education",
                "VN",
                "REG-123",
                "Owner",
                "owner@example.org",
                null));
    for (int n = 0; n < 5; n++) {
      byte[] bytes =
          n < 3
              ? new byte[] {
                (byte) 137,
                80,
                78,
                71,
                13,
                10,
                26,
                10,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                (byte) (duplicateImages ? 1 : n)
              }
              : ("%PDF-1.4 document " + n).getBytes(java.nio.charset.StandardCharsets.US_ASCII);
      UUID file =
          (UUID)
              media
                  .upload(
                      owner,
                      "ORGANIZATION",
                      org.id(),
                      new MockMultipartFile(
                          "file", "evidence-" + n, "application/octet-stream", bytes))
                  .get("id");
      evidence.add(
          owner,
          org.id(),
          file,
          n < 3 ? "COMPANY_IMAGE" : n == 3 ? "LEGAL_IDENTITY" : "REPRESENTATIVE",
          "VERIFICATION",
          "Evidence " + n,
          null);
    }
    if (approve) {
      organizations.submit(owner);
      for (var d : evidence.list(owner, org.id()))
        evidence.review(admin, org.id(), d.getId(), "ACCEPTED", "Reviewed");
      organizations.review(
          org.id(),
          admin,
          new OrganizationService.Review(Organization.Status.APPROVED, "Verified"));
    }
    return new Org(owner, admin, org.id());
  }

  record Course(UUID id, UUID version, UUID module, UUID lesson, UUID required, UUID optional) {}

  Course draft(Org org) {
    UUID id =
        (UUID)
            courses
                .create(org.owner(), org.id(), "course-" + UUID.randomUUID(), "Course", "Summary")
                .get("id");
    UUID version =
        versions
            .findFirstByCourseIdAndStatusOrderByVersionNoDesc(id, "DRAFT")
            .orElseThrow()
            .getId();
    UUID module = (UUID) content.module(org.owner(), version, 1, "Module").get("id");
    UUID lesson = lessons.create(org.owner(), module, 1, "Lesson", "Private lesson body").getId();
    UUID required =
        (UUID)
            lessons
                .resource(
                    org.owner(),
                    lesson,
                    1,
                    "ARTICLE",
                    "Required",
                    "Required body",
                    null,
                    true,
                    false)
                .get("id");
    UUID optional =
        (UUID)
            lessons
                .resource(
                    org.owner(),
                    lesson,
                    2,
                    "ARTICLE",
                    "Optional",
                    "Preview body",
                    null,
                    false,
                    true)
                .get("id");
    return new Course(id, version, module, lesson, required, optional);
  }

  @Test
  void verificationRequiresDifferentImageBytesNotRepeatedUploads() {
    var org = organization(false, true);
    assertThatThrownBy(() -> organizations.submit(org.owner()))
        .isInstanceOf(com.skillproof.backend.common.exception.BadRequestException.class);
  }

  @Test
  void acceptedSnapshotAndLegalReverificationPreserveHistory() {
    var org = organization(true, false);
    var revisions = organizations.applicationRevisions(org.owner(), org.id());
    assertThat(organizations.evidenceSnapshot(org.admin(), revisions.getFirst().id())).hasSize(5);
    assertThat(organizations.beginLegalReverification(org.owner(), org.id()).status())
        .isEqualTo(Organization.Status.DRAFT);
    assertThat(organizations.applicationRevisions(org.owner(), org.id()).getFirst().status())
        .isEqualTo("APPROVED");
  }

  @Test
  void finalVerificationEvidenceCannotBeReviewedAgain() {
    var org = organization(true, false);
    UUID doc = evidence.list(org.owner(), org.id()).getFirst().getId();
    assertThatThrownBy(
            () -> evidence.review(org.admin(), org.id(), doc, "REJECTED", "Changed mind"))
        .isInstanceOf(com.skillproof.backend.common.exception.ConflictException.class);
  }

  @Test
  void organizationPublishesWithoutAdminCourseApproval() {
    var org = organization(true, false);
    var course = draft(org);
    assertThat(readiness.check(org.owner(), course.version()).get("ready")).isEqualTo(true);
    courses.publish(org.owner(), course.version());
    assertThat(versions.findById(course.version()).orElseThrow().getStatus())
        .isEqualTo("PUBLISHED");
  }

  @Test
  void optionalResourceDoesNotBlockCompletionAndContentHasLessons() {
    var org = organization(true, false);
    var course = draft(org);
    courses.publish(org.owner(), course.version());
    UUID learner = account(UserRole.LEARNER);
    UUID enrollment = (UUID) enrollments.enroll(learner, course.id()).get("id");
    var view = progress.course(learner, enrollment);
    assertThat((List<?>) view.get("modules")).hasSize(1);
    assertThat(enrollments.enrollment(learner, enrollment))
        .containsEntry("title", "Course")
        .containsEntry("version_no", 1);
    progress.complete(learner, enrollment, course.optional());
    assertThatThrownBy(() -> lessonProgress.complete(learner, enrollment, course.lesson()))
        .isInstanceOf(com.skillproof.backend.common.exception.ConflictException.class);
    progress.complete(learner, enrollment, course.required());
    assertThat(lessonProgress.complete(learner, enrollment, course.lesson()).completed()).isTrue();
  }

  @Test
  void previewExposesOnlySelectedResources() {
    var org = organization(true, false);
    var course = draft(org);
    courses.publish(org.owner(), course.version());
    assertThat(previews.preview(course.id()))
        .extracting(CoursePreviewService.Resource::id)
        .containsExactly(course.optional());
    assertThatThrownBy(() -> previews.requirePublicResource(course.required()))
        .isInstanceOf(com.skillproof.backend.common.exception.NotFoundException.class);
  }

  @Test
  void suspensionBlocksLearnerAccessButDoesNotDeleteEnrollment() {
    var org = organization(true, false);
    var course = draft(org);
    courses.publish(org.owner(), course.version());
    UUID learner = account(UserRole.LEARNER);
    UUID enrollment = (UUID) enrollments.enroll(learner, course.id()).get("id");
    moderation.moderate(org.admin(), course.id(), true, "Policy violation");
    assertThatThrownBy(() -> enrollments.enrollment(learner, enrollment))
        .isInstanceOf(AccessDeniedException.class);
    assertThat(enrollments.mine(learner)).hasSize(1);
    moderation.moderate(org.admin(), course.id(), false, "Resolved");
    assertThat(enrollments.enrollment(learner, enrollment)).containsEntry("id", enrollment);
  }

  @Test
  void contentAuthorityDoesNotGivePricingAuthority() {
    var org = organization(true, false);
    var course = draft(org);
    UUID outsider = account(UserRole.ORGANIZER);
    membership.addMember(org.id(), outsider, org.owner(), Instant.now(), false);
    organizations.setGrant(
        org.id(),
        org.owner(),
        outsider,
        new OrganizationService.Grant(OrganizationService.Authority.MANAGE_CONTENT, true));
    assertThatThrownBy(() -> courses.configureOffer(outsider, course.version(), "PUBLIC", 10000, 0))
        .isInstanceOf(AccessDeniedException.class);
  }

  record Assessment(UUID id, UUID question, UUID option) {}

  Assessment assessment(Org org, Course course, String kind) {
    var bank = questions.createBank(org.owner(), org.id(), "Bank");
    var q =
        questions.addQuestion(
            org.owner(),
            bank.id(),
            "2+2?",
            List.of(
                new QuizAuthoringService.Option("4", true),
                new QuizAuthoringService.Option("5", false)));
    UUID root = (UUID) ((Map<?, ?>) q.get("question")).get("id");
    UUID version = (UUID) ((Map<?, ?>) q.get("version")).get("id");
    UUID option = (UUID) ((List<Map<String, Object>>) q.get("options")).getFirst().get("id");
    UUID assessment =
        (UUID)
            assessments
                .createAssessment(org.owner(), course.version(), kind, "Assessment", 60, 70, 2)
                .get("id");
    assessments.attach(org.owner(), assessment, version, 1, 1);
    return new Assessment(assessment, root, option);
  }

  @Test
  void assessmentCanReopenOnlyBeforeCoursePublication() {
    var org = organization(true, false);
    var course = draft(org);
    var a = assessment(org, course, "PRACTICE");
    assessments.publish(org.owner(), a.id());
    assertThat(assessments.reopen(org.owner(), a.id())).containsEntry("status", "DRAFT");
    assessments.publish(org.owner(), a.id());
    courses.publish(org.owner(), course.version());
    assertThatThrownBy(() -> assessments.reopen(org.owner(), a.id()))
        .isInstanceOf(com.skillproof.backend.common.exception.ConflictException.class);
  }

  @Test
  void anotherRevisionOfSameQuestionCannotBeAddedTwice() {
    var org = organization(true, false);
    var course = draft(org);
    var a = assessment(org, course, "PRACTICE");
    var revised =
        questions.revise(
            org.owner(),
            a.question(),
            "2+2 revised?",
            List.of(
                new QuizAuthoringService.Option("4", true),
                new QuizAuthoringService.Option("6", false)));
    UUID revision = (UUID) ((Map<?, ?>) revised.get("version")).get("id");
    assertThatThrownBy(() -> assessments.attach(org.owner(), a.id(), revision, 2, 1))
        .isInstanceOf(com.skillproof.backend.common.exception.ConflictException.class);
  }

  @Test
  void timeoutScoresSavedAnswersAndPracticeReviewIsAvailable() {
    var org = organization(true, false);
    var course = draft(org);
    var a = assessment(org, course, "PRACTICE");
    assessments.publish(org.owner(), a.id());
    courses.publish(org.owner(), course.version());
    UUID learner = account(UserRole.LEARNER);
    UUID enrollment = (UUID) enrollments.enroll(learner, course.id()).get("id");
    var started = attempts.start(learner, a.id(), enrollment);
    UUID attempt = (UUID) ((Map<?, ?>) started.get("attempt")).get("id");
    var question = (Map<?, ?>) ((List<?>) started.get("questions")).getFirst();
    attempts.answer(learner, attempt, (UUID) question.get("question_version_id"), a.option());
    var row = attemptRepository.findById(attempt).orElseThrow();
    row.setStartedAt(Instant.now().minusSeconds(120));
    row.setDeadlineAt(Instant.now().minusSeconds(1));
    attemptRepository.saveAndFlush(row);
    attempts.expire(attempt);
    assertThat(attempts.result(learner, attempt))
        .containsEntry("passed", true)
        .containsEntry("timedOut", true)
        .containsEntry("kind", "PRACTICE");
    assertThat(attempts.review(learner, attempt)).hasSize(1);
    assertThat(attempts.attemptHistory(learner, enrollment).getFirst())
        .containsEntry("title", "Assessment")
        .containsEntry("kind", "PRACTICE");
  }

  @Test
  void officialAnswerKeysRemainPrivateAndSubmissionIsIdempotent() {
    var org = organization(true, false);
    var course = draft(org);
    var a = assessment(org, course, "OFFICIAL");
    assessments.publish(org.owner(), a.id());
    courses.publish(org.owner(), course.version());
    UUID learner = account(UserRole.LEARNER);
    UUID enrollment = (UUID) enrollments.enroll(learner, course.id()).get("id");
    var started = attempts.start(learner, a.id(), enrollment);
    UUID attempt = (UUID) ((Map<?, ?>) started.get("attempt")).get("id");
    assertThat(attempts.submit(learner, attempt)).isEqualTo(attempts.submit(learner, attempt));
    assertThatThrownBy(() -> attempts.review(learner, attempt))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void archivedQuestionCannotBeRevisedOrAttached() {
    var org = organization(true, false);
    var course = draft(org);
    var a = assessment(org, course, "PRACTICE");
    questions.archiveQuestion(org.owner(), a.question());
    assertThatThrownBy(
            () ->
                questions.revise(
                    org.owner(),
                    a.question(),
                    "Changed",
                    List.of(
                        new QuizAuthoringService.Option("A", true),
                        new QuizAuthoringService.Option("B", false))))
        .isInstanceOf(com.skillproof.backend.common.exception.ConflictException.class);
  }

  @Test
  void restrictedGrantRevocationBlocksFurtherLearning() {
    var org = organization(true, false);
    var course = draft(org);
    courses.configureOffer(org.owner(), course.version(), "RESTRICTED", 0, 0);
    courses.publish(org.owner(), course.version());
    UUID learner = account(UserRole.LEARNER);
    var grant =
        grants.grantOrganization(
            org.owner(), org.id(), learner, ProductType.COURSE, course.version(), null);
    UUID enrollment = (UUID) enrollments.enrollVersion(learner, course.version()).get("id");
    grants.revoke(org.owner(), org.id(), grant.getId());
    assertThat(entitlements.allowed(learner, ProductType.COURSE, course.version())).isFalse();
    assertThatThrownBy(() -> progress.complete(learner, enrollment, course.required()))
        .isInstanceOf(AccessDeniedException.class);
    assertThat(enrollments.mine(learner)).hasSize(1);
  }

  @Test
  void metadataIsVersionedAndPublishedMetadataCannotBeChanged() {
    var org = organization(true, false);
    var course = draft(org);
    metadata.save(
        org.owner(),
        course.version(),
        new CourseMetadataService.Metadata(
            "software", "BEGINNER", List.of("Java", "java", "SQL"), "Basic programming"));
    assertThat(metadata.get(org.owner(), course.version()).skills()).containsExactly("Java", "SQL");
    courses.publish(org.owner(), course.version());
    UUID clone = (UUID) courses.cloneVersion(org.owner(), course.id()).get("id");
    assertThat(metadata.get(org.owner(), clone).domainTag()).isEqualTo("software");
    assertThatThrownBy(
            () ->
                metadata.save(
                    org.owner(),
                    course.version(),
                    new CourseMetadataService.Metadata("software", "ADVANCED", List.of(), "")))
        .isInstanceOf(com.skillproof.backend.common.exception.ConflictException.class);
  }

  @Test
  void requiredResourcePolicyCannotBeBypassedWithGlobalFalseFlag() {
    var org = organization(true, false);
    var course = draft(org);
    assertThatThrownBy(() -> courses.policy(org.owner(), course.version(), false, false))
        .isInstanceOf(com.skillproof.backend.common.exception.BadRequestException.class);
  }

  @Test
  void historySurvivesGrantRevocationWithoutExposingContent() {
    var org = organization(true, false);
    var course = draft(org);
    courses.configureOffer(org.owner(), course.version(), "RESTRICTED", 0, 0);
    courses.publish(org.owner(), course.version());
    UUID learner = account(UserRole.LEARNER);
    var grant =
        grants.grantOrganization(
            org.owner(), org.id(), learner, ProductType.COURSE, course.version(), null);
    UUID enrollment = (UUID) enrollments.enrollVersion(learner, course.version()).get("id");
    progress.complete(learner, enrollment, course.required());
    grants.revoke(org.owner(), org.id(), grant.getId());
    assertThat(history.history(learner, enrollment))
        .containsEntry("title", "Course")
        .doesNotContainKey("modules");
    assertThatThrownBy(() -> history.history(account(UserRole.LEARNER), enrollment))
        .isInstanceOf(com.skillproof.backend.common.exception.NotFoundException.class);
  }

  @Test
  void repeatedProgressRecordsOnlyOneLearningSignal() {
    var org = organization(true, false);
    var course = draft(org);
    courses.publish(org.owner(), course.version());
    UUID learner = account(UserRole.LEARNER);
    UUID enrollment = (UUID) enrollments.enroll(learner, course.id()).get("id");
    long before = signals.count();
    progress.complete(learner, enrollment, course.required());
    long after = signals.count();
    progress.complete(learner, enrollment, course.required());
    assertThat(after).isEqualTo(before + 1);
    assertThat(signals.count()).isEqualTo(after);
  }

  @Test
  void archivedQuestionReferencesRemainUsableWhenCloningCourse() {
    var org = organization(true, false);
    var course = draft(org);
    var a = assessment(org, course, "PRACTICE");
    assessments.publish(org.owner(), a.id());
    courses.publish(org.owner(), course.version());
    questions.archiveQuestion(org.owner(), a.question());
    UUID clone = (UUID) courses.cloneVersion(org.owner(), course.id()).get("id");
    var copied = assessments.assessments(org.owner(), clone);
    assertThat(copied).hasSize(1);
    assertThat(assessments.assessmentQuestions(org.owner(), (UUID) copied.getFirst().get("id")))
        .hasSize(1);
  }

  @Test
  void adminMayReviewButCannotRewriteOrganizationCompletionPolicy() {
    var org = organization(true, false);
    var course = draft(org);
    assertThat(courses.outline(org.admin(), course.version())).containsKey("policy");
    assertThatThrownBy(() -> courses.policy(org.admin(), course.version(), true, false))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void aNewRevisionRequiresFreshEvidenceDecisions() {
    var org = organization(true, false);
    organizations.beginLegalReverification(org.owner(), org.id());
    organizations.submit(org.owner());
    assertThat(
            organizations.evidenceSnapshot(
                org.admin(),
                organizations.applicationRevisions(org.owner(), org.id()).getFirst().id()))
        .allMatch(s -> "UPLOADED".equals(s.status()));
    assertThatThrownBy(
            () ->
                organizations.review(
                    org.id(),
                    org.admin(),
                    new OrganizationService.Review(
                        Organization.Status.APPROVED, "Approve without reviewing again")))
        .isInstanceOf(com.skillproof.backend.common.exception.BadRequestException.class);
  }
}
