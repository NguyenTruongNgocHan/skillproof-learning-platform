package com.skillproof.backend.course;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.assignment.application.AssignmentAuthoringService;
import com.skillproof.backend.assignment.application.AssignmentSubmissionService;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.course.application.CourseAuthoringService;
import com.skillproof.backend.course.application.CourseContentService;
import com.skillproof.backend.course.application.CourseEnrollmentService;
import com.skillproof.backend.course.application.CourseProgressService;
import com.skillproof.backend.course.application.LessonProgressService;
import com.skillproof.backend.course.application.LessonService;
import com.skillproof.backend.course.infrastructure.CourseContextRepository;
import com.skillproof.backend.identity.IdentityTestMailConfig;
import com.skillproof.backend.identity.domain.UserAccount;
import com.skillproof.backend.identity.domain.UserRole;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;

@SpringBootTest
@ActiveProfiles("test")
@Import(IdentityTestMailConfig.class)
@Transactional
class CourseActivityFlowIntegrationTest {

    @Autowired
    UserAccountRepository accounts;
    @Autowired
    CourseAuthoringService authoring;
    @Autowired
    CourseContentService content;
    @Autowired
    LessonService lessons;
    @Autowired
    CourseEnrollmentService enrollments;
    @Autowired
    CourseProgressService progress;
    @Autowired
    LessonProgressService lessonProgress;
    @Autowired
    CourseContextRepository versions;
    @Autowired
    AssignmentAuthoringService assignments;
    @Autowired
    AssignmentSubmissionService submissions;

    private UUID account(UserRole role) {
        String email = UUID.randomUUID() + "@example.org";
        Instant now = Instant.now();

        UserAccount user;
        if (role == UserRole.ADMIN) {
            user = UserAccount.newAdmin(
                    email, "test-password-hash", "Admin", now);
        } else {
            user = UserAccount.newAccount(
                    email, "test-password-hash", "User", role);
            user.verifyEmail(now);
        }

        return accounts.saveAndFlush(user).getId();
    }

    private record Course(UUID id, UUID version, UUID lesson, UUID resource) {}

    private Course draft(UUID owner) {
        UUID id = (UUID) authoring.create(owner, null, "course-" + UUID.randomUUID(), "Course", "Summary").get("id");
        UUID version = versions.findFirstByCourseIdAndStatusOrderByVersionNoDesc(id, "DRAFT").orElseThrow().getId();
        UUID module = (UUID) content.module(owner, version, 1, "Module").get("id");
        UUID lesson = lessons.create(owner, module, 1, "Lesson", "Lesson text").getId();
        UUID resource = (UUID) lessons.resource(owner, lesson, 1, "ARTICLE", "Reading", "Read this", null, true, false).get("id");
        return new Course(id, version, lesson, resource);
    }

    private void publish(UUID owner, UUID admin, Course course) {
        authoring.submitReview(owner, course.version());
        authoring.review(admin, course.version(), true, "Content approved");
        authoring.publish(owner, course.version());
    }

    @Test
    void requiredAssignmentBlocksCompletionUntilGradedPass() {
        UUID owner = account(UserRole.LEARNER), admin = account(UserRole.ADMIN), learner = account(UserRole.LEARNER);
        var course = draft(owner);
        var assignment = assignments.create(owner, course.version(), "LESSON", course.lesson(), "Exercise", "Submit your answer", true, 70, 2, null);
        publish(owner, admin, course);
        UUID enrollment = (UUID) enrollments.enroll(learner, course.id()).get("id");
        assertThrows(ConflictException.class, () -> lessonProgress.complete(learner, enrollment, course.lesson()));
        progress.complete(learner, enrollment, course.resource());
        assertFalse(lessonProgress.complete(learner, enrollment, course.lesson()).completed());
        var submission = submissions.prepare(learner, assignment.getId(), enrollment, "Initial answer", null);
        submissions.editDraft(learner, submission.getId(), "Final answer", null);
        submissions.submit(learner, submission.getId());
        assertTrue(submissions.grade(owner, submission.getId(), 80, "Meets requirements").getPassed());
        assertTrue(lessonProgress.complete(learner, enrollment, course.lesson()).completed());
        assertThrows(ConflictException.class, () -> submissions.editDraft(learner, submission.getId(), "Changed", null));
    }

    @Test
    void paidCourseCannotBeEnrolledWithoutStudyEntitlement() {
        UUID owner = account(UserRole.LEARNER), admin = account(UserRole.ADMIN), learner = account(UserRole.LEARNER);
        var course = draft(owner);
        authoring.configureOffer(owner, course.version(), "PUBLIC", 10000, 0);
        publish(owner, admin, course);
        assertThrows(AccessDeniedException.class, () -> enrollments.enroll(learner, course.id()));
    }

    @Test
    void publishedContentIsImmutableAndCloneDoesNotMoveExistingEnrollment() {
        UUID owner = account(UserRole.LEARNER), admin = account(UserRole.ADMIN), learner = account(UserRole.LEARNER);
        var course = draft(owner);
        publish(owner, admin, course);
        UUID enrollment = (UUID) enrollments.enroll(learner, course.id()).get("id");
        assertThrows(ConflictException.class, () -> content.module(owner, course.version(), 2, "Forbidden mutation"));
        UUID clone = (UUID) authoring.cloneVersion(owner, course.id()).get("id");
        assertNotEquals(course.version(), clone);
        assertEquals(course.version(), enrollments.enrollment(learner, enrollment).get("version_id"));
        assertEquals("PUBLISHED", versions.findById(course.version()).orElseThrow().getStatus());
        assertEquals("DRAFT", versions.findById(clone).orElseThrow().getStatus());
    }
}
