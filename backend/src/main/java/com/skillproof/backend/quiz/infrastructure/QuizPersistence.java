package com.skillproof.backend.quiz.application;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "question_version")
class QuizQuestionVersion {

    @Id
    UUID id;
    @Column(name = "question_id", nullable = false)
    UUID questionId;
    @Column(name = "version_no", nullable = false)
    int versionNo;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;
    @Column(nullable = false)
    String stem;
}

@Entity
@Table(name = "question_option")
class QuizOption {

    @Id
    UUID id;
    @Column(name = "question_version_id", nullable = false)
    UUID questionVersionId;
    @Column(nullable = false)
    String body;
    @Column(nullable = false)
    boolean correct;
    @Column(nullable = false)
    int position;
}

@Entity
@Table(name = "question_bank")
class QuizBank {

    @Id
    UUID id;
    @Column(name = "organization_id", nullable = false)
    UUID organizationId;
    @Column(nullable = false)
    String title;
    @Column(name = "created_by", nullable = false)
    UUID createdBy;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;
}

@Entity
@Table(name = "question")
class QuizQuestion {

    @Id
    UUID id;
    @Column(name = "bank_id", nullable = false)
    UUID bankId;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;
}

@Entity
@Table(name = "assessment")
class QuizAssessment {

    @Id
    UUID id;
    @Column(name = "organization_id", nullable = false)
    UUID organizationId;
    @Column(name = "version_id", nullable = false)
    UUID versionId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Status status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Kind kind;
    @Column(nullable = false)
    String title;
    @Column(name = "duration_seconds", nullable = false)
    int durationSeconds;
    @Column(name = "pass_percent", nullable = false)
    int passPercent;
    @Column(name = "max_attempts", nullable = false)
    int maxAttempts;
    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    enum Status {
        DRAFT, PUBLISHED
    }

    enum Kind {
        PRACTICE, MOCK, OFFICIAL
    }
}

@Entity
@Table(name = "assessment_question")
class QuizAssessmentQuestion {

    @EmbeddedId
    QuizAssessmentQuestionId id;
    @ManyToOne
    @MapsId("assessmentId")
    @JoinColumn(name = "assessment_id")
    QuizAssessment assessment;
    @ManyToOne
    @MapsId("questionVersionId")
    @JoinColumn(name = "question_version_id")
    QuizQuestionVersion questionVersion;
    int position;
    int points;
}

@Embeddable
class QuizAssessmentQuestionId {

    UUID assessmentId;
    UUID questionVersionId;

    QuizAssessmentQuestionId() {
    }

    QuizAssessmentQuestionId(UUID a, UUID q) {
        assessmentId = a;
        questionVersionId = q;
    }

    public boolean equals(Object o) {
        return o instanceof QuizAssessmentQuestionId x && Objects.equals(assessmentId, x.assessmentId) && Objects.equals(questionVersionId, x.questionVersionId);
    }

    public int hashCode() {
        return Objects.hash(assessmentId, questionVersionId);
    }
}

interface QuizQuestionRepository extends JpaRepository<QuizQuestionVersion, UUID> {

    @org.springframework.data.jpa.repository.Query("select distinct q from QuizQuestionVersion q join QuizAssessmentQuestion aq on aq.questionVersion=q join aq.assessment a where a.status= :status order by q.id")
    List<QuizQuestionVersion> published(QuizAssessment.Status status, org.springframework.data.domain.Pageable page);

    @org.springframework.data.jpa.repository.Query("select distinct q from QuizQuestionVersion q join QuizAssessmentQuestion aq on aq.questionVersion=q join aq.assessment a where q.id=:id and a.status= :status")
    Optional<QuizQuestionVersion> published(UUID id, QuizAssessment.Status status);
}

interface QuizBankRepository extends JpaRepository<QuizBank, UUID> {

    List<QuizBank> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}

interface QuizQuestionRootRepository extends JpaRepository<QuizQuestion, UUID> {

    List<QuizQuestion> findByBankIdOrderByCreatedAtDesc(UUID bankId);
}

interface QuizQuestionVersionRepository extends JpaRepository<QuizQuestionVersion, UUID> {

    Optional<QuizQuestionVersion> findFirstByQuestionIdOrderByVersionNoDesc(UUID questionId);

    @org.springframework.data.jpa.repository.Query("select max(q.versionNo) from QuizQuestionVersion q where q.questionId=:questionId")
    Integer maxVersion(UUID questionId);
}

interface QuizOptionRepository extends JpaRepository<QuizOption, UUID> {

    List<QuizOption> findByQuestionVersionIdOrderByPosition(UUID questionVersionId);
}

interface QuizAssessmentRepository extends JpaRepository<QuizAssessment, UUID> {

    List<QuizAssessment> findByVersionIdOrderByCreatedAtDesc(UUID versionId);

    List<QuizAssessment> findByVersionIdAndStatus(UUID versionId, QuizAssessment.Status status);

    boolean existsByVersionIdAndStatus(UUID versionId, QuizAssessment.Status status);

    boolean existsByVersionIdAndKindAndStatus(UUID versionId, QuizAssessment.Kind kind, QuizAssessment.Status status);
}

interface QuizAssessmentQuestionRepository extends JpaRepository<QuizAssessmentQuestion, QuizAssessmentQuestionId> {

    List<QuizAssessmentQuestion> findByAssessmentIdOrderByPosition(UUID assessmentId);

    void deleteByAssessmentId(UUID assessmentId);
}

@Entity
@Table(name = "assessment_attempt")
class QuizAttempt {

    @Id
    UUID id;
    @Column(name = "assessment_id", nullable = false)
    UUID assessmentId;
    @Column(name = "enrollment_id", nullable = false)
    UUID enrollmentId;
    @Column(name = "learner_id", nullable = false)
    UUID learnerId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AttemptStatus status;
    @Column(name = "started_at", nullable = false)
    Instant startedAt;
    @Column(name = "deadline_at", nullable = false)
    Instant deadlineAt;
    @Column(name = "submitted_at")
    Instant submittedAt;
    @Column(name = "score_percent")
    Integer scorePercent;
    Boolean passed;

    enum AttemptStatus {
        IN_PROGRESS, SUBMITTED, TIMED_OUT, SCORED
    }
}

@Entity
@Table(name = "attempt_question")
class QuizAttemptQuestion {

    @EmbeddedId
    QuizAttemptQuestionId id;
    @Column(nullable = false)
    int position;
    @Column(name = "points", nullable = false)
    int points;
    @Column(name = "selected_option_id")
    UUID selectedOptionId;
}

@Embeddable
class QuizAttemptQuestionId {

    UUID attemptId;
    UUID questionVersionId;

    QuizAttemptQuestionId() {
    }

    QuizAttemptQuestionId(UUID a, UUID q) {
        attemptId = a;
        questionVersionId = q;
    }

    public boolean equals(Object o) {
        return o instanceof QuizAttemptQuestionId x && Objects.equals(attemptId, x.attemptId) && Objects.equals(questionVersionId, x.questionVersionId);
    }

    public int hashCode() {
        return Objects.hash(attemptId, questionVersionId);
    }
}

interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

    List<QuizAttempt> findByAssessmentIdAndEnrollmentIdAndStatus(UUID a, UUID e, QuizAttempt.AttemptStatus s);

    long countByAssessmentIdAndEnrollmentId(UUID a, UUID e);

    List<QuizAttempt> findByEnrollmentIdAndLearnerIdOrderByStartedAtDesc(UUID e, UUID l);

    long countByAssessmentIdInAndEnrollmentIdAndPassedTrue(Collection<UUID> assessmentIds, UUID enrollmentId);
}

interface QuizAttemptQuestionRepository extends JpaRepository<QuizAttemptQuestion, QuizAttemptQuestionId> {

    List<QuizAttemptQuestion> findByIdAttemptIdOrderByPosition(UUID attemptId);
}
