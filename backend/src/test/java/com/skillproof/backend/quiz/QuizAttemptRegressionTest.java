package com.skillproof.backend.quiz;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.skillproof.backend.quiz.application.QuizAttemptService;
import com.skillproof.backend.quiz.infrastructure.persistence.*;
import com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery;
import com.skillproof.backend.course.contract.CourseQuizAccess;
class QuizAttemptRegressionTest {
    private final QuizAssessmentRepository assessments=mock(QuizAssessmentRepository.class);
    private final QuizAttemptRepository attempts=mock(QuizAttemptRepository.class);
    private final QuizAttemptQuestionRepository questions=mock(QuizAttemptQuestionRepository.class);
    private final CourseQuizAccess learning=mock(CourseQuizAccess.class);
    private final QuizAttemptService service=new QuizAttemptService(assessments,attempts,questions,learning,
        mock(QuizCompletionEvidenceQuery.class),mock(QuizAssessmentQuestionRepository.class),mock(QuizOptionRepository.class),mock(QuizQuestionRepository.class));
    private final UUID learner=UUID.randomUUID(), enrollment=UUID.randomUUID();
    private QuizAttempt fixture(boolean expired) {
        var attempt=new QuizAttempt();attempt.setId(UUID.randomUUID());attempt.setLearnerId(learner);
        attempt.setEnrollmentId(enrollment);attempt.setAssessmentId(UUID.randomUUID());
        attempt.setStatus(QuizAttempt.AttemptStatus.IN_PROGRESS);attempt.setStartedAt(Instant.now().minusSeconds(120));
        attempt.setDeadlineAt(expired?Instant.now().minusSeconds(1):Instant.now().plusSeconds(60));
        when(attempts.findById(attempt.getId())).thenReturn(Optional.of(attempt));
        when(attempts.findForUpdate(attempt.getId())).thenReturn(Optional.of(attempt));
        var assessment=new QuizAssessment();assessment.setKind(QuizAssessment.Kind.PRACTICE);assessment.setPassPercent(70);
        when(assessments.findById(attempt.getAssessmentId())).thenReturn(Optional.of(assessment));
        when(questions.findByIdAttemptIdOrderByPosition(attempt.getId())).thenReturn(List.of());
        return attempt;
    }
    @org.junit.jupiter.api.BeforeEach
    void initializePersistenceContext() {
        org.springframework.test.util.ReflectionTestUtils.setField(service,"entityManager",mock(jakarta.persistence.EntityManager.class));
    }

    @Test void historyAllowsUnscoredNullFields() {
        var attempt=fixture(false);
        when(attempts.findByEnrollmentIdAndLearnerIdOrderByStartedAtDesc(enrollment,learner)).thenReturn(List.of(attempt));
        var history=service.attemptHistory(learner,enrollment);
        assertThat(history.get(0)).containsEntry("score_percent",null).containsEntry("passed",null).containsEntry("submitted_at",null);
    }
    @Test void expiredSubmitReturnsTimedOut() {
        var attempt=fixture(true);var result=service.submit(learner,attempt.getId());
        assertThat(result).containsEntry("timedOut",true);assertThat(attempt.getStatus()).isEqualTo(QuizAttempt.AttemptStatus.TIMED_OUT);
        verify(learning).enrollment(learner,enrollment,true);verify(attempts).findForUpdate(attempt.getId());
    }
    @Test void repeatedSubmitDoesNotScoreAgain() {
        var attempt=fixture(false);service.submit(learner,attempt.getId());service.submit(learner,attempt.getId());
        verify(attempts,times(1)).saveAndFlush(attempt);
    }
    @Test void finalizedAttemptRejectsAnswer() {
        var attempt=fixture(false);attempt.setStatus(QuizAttempt.AttemptStatus.SCORED);
        assertThatThrownBy(()->service.answer(learner,attempt.getId(),UUID.randomUUID(),UUID.randomUUID()))
            .isInstanceOf(com.skillproof.backend.common.exception.ConflictException.class);
    }
    @Test void otherLearnerCannotSubmit() {
        var attempt=fixture(false);
        assertThatThrownBy(()->service.submit(UUID.randomUUID(),attempt.getId()))
            .isInstanceOf(com.skillproof.backend.common.exception.NotFoundException.class);
        verify(attempts,never()).findForUpdate(any());
    }
}
