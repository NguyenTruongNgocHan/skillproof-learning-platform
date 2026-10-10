package com.skillproof.backend.certification;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.skillproof.backend.certification.application.*;
import com.skillproof.backend.certification.domain.*;
import com.skillproof.backend.course.contract.*;
import com.skillproof.backend.organization.contract.OrganizationAuthorityQuery;
import com.skillproof.backend.quiz.contract.QuizCompletionEvidenceQuery;
import tools.jackson.databind.ObjectMapper;
class EligibilityServiceTest {
    private final CertificationProgramRepository programs=mock(CertificationProgramRepository.class);
    private final CertificateEligibilityRepository evaluations=mock(CertificateEligibilityRepository.class);
    private final CompletionEvidenceQuery completion=mock(CompletionEvidenceQuery.class);
    private final QuizCompletionEvidenceQuery quiz=mock(QuizCompletionEvidenceQuery.class);
    private final OrganizationAuthorityQuery authority=mock(OrganizationAuthorityQuery.class);
    private final EligibilityService service=new EligibilityService(programs,evaluations,mock(CertificationContextQuery.class),completion,authority,quiz,new ObjectMapper(),mock(com.skillproof.backend.access.contract.AccessEntitlementQuery.class));
    @Test void completedLearnerIsEligible() {
        UUID learner=UUID.randomUUID(),version=UUID.randomUUID(),policy=UUID.randomUUID(),enrollment=UUID.randomUUID(),program=UUID.randomUUID();
        var value=new CertificationProgram(program,UUID.randomUUID(),version,policy,"Program",CertificationProgram.Status.ACTIVE,UUID.randomUUID(),Instant.now());
        when(programs.findById(program)).thenReturn(Optional.of(value));
        when(quiz.evidence(version,enrollment)).thenReturn(new QuizCompletionEvidenceQuery.AssessmentEvidence(1,1));
        when(completion.evaluate(enrollment,1,1)).thenReturn(new CompletionEvidenceQuery.Evidence(UUID.randomUUID(),enrollment,learner,version,policy,1,1,1,1,0,0,1,1,true));
        when(evaluations.save(any())).thenAnswer(call -> call.getArgument(0));
        assertThat(service.evaluate(learner,program,enrollment).status()).isEqualTo(CertificateEligibility.Status.ELIGIBLE);
    }
}
