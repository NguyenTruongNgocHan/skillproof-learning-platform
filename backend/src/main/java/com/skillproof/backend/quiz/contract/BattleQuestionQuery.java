package com.skillproof.backend.quiz.contract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public read-only Quiz contract used by Realtime. Realtime receives immutable
 * question-version projections and never accesses Quiz tables.
 */
public interface BattleQuestionQuery {

    record Option(UUID id, String text) {

    }

    record Question(UUID questionVersionId, String stem, List<Option> options) {

    }

    List<Question> selectPublishedQuestions(String skillContext, int limit);

    Optional<Question> findPublishedQuestion(UUID questionVersionId);

    boolean isCorrectOption(UUID questionVersionId, UUID optionId);
}
