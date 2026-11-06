package com.skillproof.backend.quiz;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.quiz.application.QuizAuthoringService;
import com.skillproof.backend.quiz.infrastructure.persistence.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuizRulesTest {

  @Test
  void questionNeedsExactlyOneCorrectOption() {
    assertThrows(
        BadRequestException.class,
        () ->
            QuizAuthoringService.validateOptions(
                List.of(
                    new QuizAuthoringService.Option("A", false),
                    new QuizAuthoringService.Option("B", false))));
    assertThrows(
        BadRequestException.class,
        () ->
            QuizAuthoringService.validateOptions(
                List.of(
                    new QuizAuthoringService.Option("A", true),
                    new QuizAuthoringService.Option("B", true))));
    assertDoesNotThrow(
        () ->
            QuizAuthoringService.validateOptions(
                List.of(
                    new QuizAuthoringService.Option("A", true),
                    new QuizAuthoringService.Option("B", false))));
  }

  @Test
  void questionRejectsBlankOrTooFewChoices() {
    assertThrows(
        BadRequestException.class,
        () ->
            QuizAuthoringService.validateOptions(
                List.of(new QuizAuthoringService.Option("A", true))));
    assertThrows(
        BadRequestException.class,
        () ->
            QuizAuthoringService.validateOptions(
                List.of(
                    new QuizAuthoringService.Option("A", true),
                    new QuizAuthoringService.Option(" ", false))));
  }
}
