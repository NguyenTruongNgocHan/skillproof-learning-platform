package com.skillproof.backend.course;

import static org.junit.jupiter.api.Assertions.*;

import com.skillproof.backend.course.domain.CompletionRules;
import org.junit.jupiter.api.Test;

class CompletionRulesTest {
  @Test
  void courseCanCompleteWithoutCertificationOrAssessments() {
    assertTrue(CompletionRules.completed(true, false, 1, 1, 0, 0, 0, 0, 1, 1));
  }

  @Test
  void requiredAssessmentsAlwaysApply() {
    assertFalse(CompletionRules.completed(true, false, 1, 1, 1, 0, 0, 0, 1, 1));
  }

  @Test
  void impossibleCountersNeverComplete() {
    assertFalse(CompletionRules.completed(true, false, 1, 2, 0, 0, 0, 0, 1, 1));
    assertFalse(CompletionRules.completed(true, false, 1, 1, 0, 0, 0, 0, 0, 0));
  }
}
