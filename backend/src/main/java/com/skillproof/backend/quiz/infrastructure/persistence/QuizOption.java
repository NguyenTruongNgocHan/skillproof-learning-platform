package com.skillproof.backend.quiz.infrastructure.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "question_option")
public class QuizOption {

    @Id
    private UUID id;
    @Column(name = "question_version_id", nullable = false)
    private UUID questionVersionId;
    @Column(nullable = false)
    private String body;
    @Column(nullable = false)
    private boolean correct;
    @Column(nullable = false)
    private int position;

    public UUID getId() {
        return id;
    }

    public void setId(UUID value) {
        id = value;
    }

    public UUID getQuestionVersionId() {
        return questionVersionId;
    }

    public void setQuestionVersionId(UUID value) {
        questionVersionId = value;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String value) {
        body = value;
    }

    public boolean getCorrect() {
        return correct;
    }

    public void setCorrect(boolean value) {
        correct = value;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int value) {
        position = value;
    }
}
