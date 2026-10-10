package com.skillproof.backend.quiz.infrastructure.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "attempt_question")
public class QuizAttemptQuestion {

    @EmbeddedId
    private QuizAttemptQuestionId id;
    @Column(nullable = false)
    private int position;
    @Column(name = "points", nullable = false)
    private int points;
    @Column(name = "selected_option_id")
    private UUID selectedOptionId;

    public QuizAttemptQuestionId getId() {
        return id;
    }

    public void setId(QuizAttemptQuestionId value) {
        id = value;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int value) {
        position = value;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int value) {
        points = value;
    }

    public UUID getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(UUID value) {
        selectedOptionId = value;
    }
}
