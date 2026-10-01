package com.skillproof.backend.realtime.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.skillproof.backend.realtime.domain.BattleState;

/**
 * Realtime persistence port. It exposes only Realtime-owned concepts.
 */
public interface BattleRepository {

    record QuestionSeed(UUID questionVersionId) {

    }

    record Participant(UUID id, UUID userId, double rating, double score) {

    }

    record Battle(UUID id, String skillContext, BattleState state, long sequence, List<Participant> participants) {

    }

    record AnswerEffect(boolean accepted, String code, double awarded, double score) {

    }

    Battle create(String skillContext, UUID firstLearner, UUID secondLearner, List<QuestionSeed> questions);

    Battle get(UUID battleId);

    long nextSequence(UUID battleId);

    Battle transition(UUID battleId, BattleState expected, BattleState next);

    void ready(UUID battleId, UUID userId);

    boolean bothReady(UUID battleId);

    UUID questionVersionId(UUID battleId, int position);

    int questionCount(UUID battleId);

    AnswerEffect answer(UUID battleId, UUID userId, UUID questionVersionId, UUID submissionId,
            UUID optionId, boolean correct, Instant submittedAt);

    Map<String, Object> finish(UUID battleId, String reason);

    boolean participates(UUID battleId, UUID userId);

    List<Map<String, Object>> ratings(UUID userId);

    List<Map<String, Object>> leaderboard(String skillContext, int limit);

    List<Map<String, Object>> history(UUID userId);

    Map<String, Object> detail(UUID battleId, UUID userId);
}
