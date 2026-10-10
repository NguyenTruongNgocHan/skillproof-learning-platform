package com.skillproof.backend.quiz.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionVersionRepository extends JpaRepository<QuizQuestionVersion, UUID> {

    Optional<QuizQuestionVersion> findFirstByQuestionIdOrderByVersionNoDesc(UUID questionId);

    @org.springframework.data.jpa.repository.Query("select max(q.versionNo) from QuizQuestionVersion q where q.questionId=:questionId")
    Integer maxVersion(UUID questionId);
}
