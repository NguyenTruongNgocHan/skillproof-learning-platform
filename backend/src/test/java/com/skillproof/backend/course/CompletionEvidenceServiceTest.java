package com.skillproof.backend.course;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.skillproof.backend.course.application.*;
import com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery;
import tools.jackson.databind.ObjectMapper;
class CompletionEvidenceServiceTest {
    private final CompletionEvidenceStore store=mock(CompletionEvidenceStore.class);
    private final QuizCompletionEvidenceQuery assessments=mock(QuizCompletionEvidenceQuery.class);
    private final com.skillproof.backend.assignment.contract.AssignmentEvidenceQuery assignments=mock(com.skillproof.backend.assignment.contract.AssignmentEvidenceQuery.class);
    private final com.skillproof.backend.course.contract.LessonEvidenceQuery lessons=mock(com.skillproof.backend.course.contract.LessonEvidenceQuery.class);
    private final CompletionEvidenceService service=new CompletionEvidenceService(store,new ObjectMapper(),assessments,assignments,lessons);
    private final UUID enrollment=UUID.randomUUID(), learner=UUID.randomUUID(), version=UUID.randomUUID(), policy=UUID.randomUUID();
    private void fixture(int required,int passed,long done) {
        when(assignments.evidence(version,enrollment)).thenReturn(new com.skillproof.backend.assignment.contract.AssignmentEvidenceQuery.Evidence(0,0));
        when(lessons.evidence(version,enrollment)).thenReturn(new com.skillproof.backend.course.contract.LessonEvidenceQuery.Evidence(1,1));
        when(store.lockEnrollment(enrollment)).thenReturn(new CompletionEvidenceStore.Enrollment(enrollment,learner,version));
        when(store.resourceCount(version)).thenReturn(2L);when(store.completedResourceCount(enrollment)).thenReturn(done);
        when(store.policy(version)).thenReturn(new CompletionEvidenceStore.Policy(policy,true,true));
        when(assessments.evidence(version,enrollment)).thenReturn(new QuizCompletionEvidenceQuery.AssessmentEvidence(required,passed));
    }
    @Test void ignoresPlaceholderCountsAndUsesAuthoritativeQuizEvidence() {
        fixture(2,2,2);var evidence=service.evaluate(enrollment,0,0);
        assertThat(evidence.completed()).isTrue();assertThat(evidence.requiredAssessments()).isEqualTo(2);
        assertThat(evidence.passedAssessments()).isEqualTo(2);verify(store).markCompleted(eq(enrollment),any());
    }
    @Test void missingAssessmentDoesNotComplete() {
        fixture(2,1,2);assertThat(service.evaluate(enrollment,2,2).completed()).isFalse();
        verify(store,never()).markCompleted(any(),any());
    }
    @Test void missingResourceDoesNotComplete() {
        fixture(2,2,1);assertThat(service.evaluate(enrollment,0,0).completed()).isFalse();
    }
    @Test void readsReevaluateInsteadOfReturningStaleSnapshot() {
        fixture(2,2,2);when(store.belongsToLearner(enrollment,learner)).thenReturn(true);
        assertThat(service.readForLearner(enrollment,learner).completed()).isTrue();
        verify(store,never()).latestEvidenceJson(any());
    }
    @Test void requiredAssessmentCannotBeBypassedByOptionalPolicy() {
        fixture(1,0,2);
        when(store.policy(version)).thenReturn(new CompletionEvidenceStore.Policy(policy,true,false));
        assertThat(service.evaluate(enrollment,0,0).completed()).isFalse();
    }
    @Test void requiredAssignmentMustPass() {
        fixture(1,1,2);
        when(assignments.evidence(version,enrollment)).thenReturn(new com.skillproof.backend.assignment.contract.AssignmentEvidenceQuery.Evidence(2,1));
        assertThat(service.evaluate(enrollment,0,0).completed()).isFalse();
    }
    @Test void emptyCourseCannotComplete() {
        fixture(1,1,2);
        when(lessons.evidence(version,enrollment)).thenReturn(new com.skillproof.backend.course.contract.LessonEvidenceQuery.Evidence(0,0));
        assertThat(service.evaluate(enrollment,0,0).completed()).isFalse();
    }
    @Test void lessonMustBeCompletedEvenWithoutAnAssessmentPolicy() {
        fixture(0,0,2);
        when(store.policy(version)).thenReturn(new CompletionEvidenceStore.Policy(policy,true,false));
        when(lessons.evidence(version,enrollment)).thenReturn(new com.skillproof.backend.course.contract.LessonEvidenceQuery.Evidence(2,1));
        assertThat(service.evaluate(enrollment,0,0).completed()).isFalse();
    }
}
