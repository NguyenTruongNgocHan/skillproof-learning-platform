package com.skillproof.backend.quiz.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.skillproof.backend.quiz.contract.BattleQuestionQuery;

@Service
public class BattleQuestionQueryService implements BattleQuestionQuery {

    private final QuizQuestionRepository questions;
    private final QuizOptionRepository options;

    public BattleQuestionQueryService(QuizQuestionRepository questions, QuizOptionRepository options) {
        this.questions = questions;
        this.options = options;
    }

    public List<Question> selectPublishedQuestions(String skillContext, int limit) {
        return questions.published(QuizAssessment.Status.PUBLISHED, PageRequest.of(0, limit)).stream().map(this::view).toList();
    }

    public Optional<Question> findPublishedQuestion(UUID id) {
        return questions.published(id, QuizAssessment.Status.PUBLISHED).map(this::view);
    }

    public boolean isCorrectOption(UUID questionVersionId, UUID optionId) {
        return questions.findById(questionVersionId).map(q -> options.findByQuestionVersionIdOrderByPosition(q.id).stream()
                .anyMatch(o -> o.id.equals(optionId) && o.correct)).orElse(false);
    }

    private Question view(QuizQuestionVersion q) {
        return new Question(q.id, q.stem, options.findByQuestionVersionIdOrderByPosition(q.id).stream()
                .map(o -> new Option(o.id, o.body)).toList());
    }
}
