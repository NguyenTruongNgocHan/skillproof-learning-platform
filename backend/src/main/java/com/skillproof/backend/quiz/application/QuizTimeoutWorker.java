package com.skillproof.backend.quiz.application;

import java.time.Instant;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.skillproof.backend.quiz.infrastructure.persistence.QuizAttempt;
import com.skillproof.backend.quiz.infrastructure.persistence.QuizAttemptRepository;

@Component
@Profile("!test")
public class QuizTimeoutWorker {

    private final QuizAttemptRepository attempts;
    private final QuizAttemptService service;

    public QuizTimeoutWorker(QuizAttemptRepository attempts, QuizAttemptService service) {
        this.attempts = attempts;
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${skillproof.quiz.timeout-reconcile-delay:60000}")
    public void reconcile() {
        for (var attempt : attempts.findByStatusAndDeadlineAtBefore(QuizAttempt.AttemptStatus.IN_PROGRESS,
                Instant.now(), PageRequest.of(0, 100))) {
            try {
                service.submit(attempt.getLearnerId(), attempt.getId());
            } catch (RuntimeException failure) {
                org.slf4j.LoggerFactory.getLogger(QuizTimeoutWorker.class).warn("Quiz timeout reconciliation failed for {}", attempt.getId(), failure);
            }
        }
    }
}
