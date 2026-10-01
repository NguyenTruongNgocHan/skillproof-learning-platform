package com.skillproof.backend.quiz;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.quiz.application.QuizService;

class QuizRulesTest {

    @Test
    void questionNeedsExactlyOneCorrectOption() {
        assertThrows(BadRequestException.class, () -> QuizService.validateOptions(List.of(
                new QuizService.Option("A", false), new QuizService.Option("B", false))));
        assertThrows(BadRequestException.class, () -> QuizService.validateOptions(List.of(
                new QuizService.Option("A", true), new QuizService.Option("B", true))));
        assertDoesNotThrow(() -> QuizService.validateOptions(List.of(
                new QuizService.Option("A", true), new QuizService.Option("B", false))));
    }

    @Test
    void questionRejectsBlankOrTooFewChoices() {
        assertThrows(BadRequestException.class, () -> QuizService.validateOptions(List.of(new QuizService.Option("A", true))));
        assertThrows(BadRequestException.class, () -> QuizService.validateOptions(List.of(
                new QuizService.Option("A", true), new QuizService.Option(" ", false))));
    }
}
