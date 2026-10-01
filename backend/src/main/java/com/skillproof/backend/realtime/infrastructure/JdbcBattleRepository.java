package com.skillproof.backend.realtime.infrastructure;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.common.exception.UnauthorizedException;
import com.skillproof.backend.realtime.application.BattleRepository;
import com.skillproof.backend.realtime.domain.BattleState;

@Repository
public class JdbcBattleRepository implements BattleRepository {

    private final BattleSessionJpaRepository sessions;
    private final BattleParticipantJpaRepository participants;
    private final BattleQuestionJpaRepository questions;
    private final BattleRatingJpaRepository ratings;
    private final BattleSubmissionJpaRepository submissions;
    private final BattleHistoryJpaRepository histories;

    public JdbcBattleRepository(BattleSessionJpaRepository s, BattleParticipantJpaRepository p, BattleQuestionJpaRepository q, BattleRatingJpaRepository r, BattleSubmissionJpaRepository sub, BattleHistoryJpaRepository h) {
        sessions = s;
        participants = p;
        questions = q;
        ratings = r;
        submissions = sub;
        histories = h;
    }

    private Battle battle(UUID id) {
        var s = sessions.findById(id).orElseThrow(() -> new NotFoundException("BATTLE_NOT_FOUND", "Battle not found"));
        var ps = participants.findByBattleIdOrderByJoinedAt(id).stream()
                .map(p -> new Participant(p.id, p.userId, p.ratingSnapshot.doubleValue(), p.finalScore.doubleValue()))
                .toList();
        return new Battle(id, s.skill, BattleState.valueOf(s.state), s.sequence, ps);
    }

    @Transactional
    public Battle create(String skill, UUID a, UUID b, List<QuestionSeed> qs) {
        var now = Instant.now();
        var s = new BattleSessionEntity();
        s.id = UUID.randomUUID();
        s.skill = skill;
        s.state = "MATCHED";
        s.sequence = 1;
        s.created = now;
        sessions.save(s);
        for (UUID u : List.of(a, b)) {
            var p = new BattleParticipantEntity();
            p.id = UUID.randomUUID();
            p.battleId = s.id;
            p.userId = u;
            var key = new BattleRatingId();
            key.learnerUserId = u;
            key.skillContext = skill;
            p.ratingSnapshot = ratings.findById(key).map(x -> x.ratingValue).orElse(BigDecimal.valueOf(1000));
            p.finalScore = BigDecimal.ZERO;
            p.joinedAt = now;
            participants.save(p);
        }
        int n = 1;
        for (var q : qs) {
            var x = new BattleQuestionEntity();
            x.id = UUID.randomUUID();
            x.battleId = s.id;
            x.questionVersionId = q.questionVersionId();
            x.position = n++;
            x.points = BigDecimal.ONE;
            questions.save(x);
        }
        return battle(s.id);
    }

    public Battle get(UUID id) {
        return battle(id);
    }

    @Transactional
    public long nextSequence(UUID id) {
        var s = sessions.findById(id).orElseThrow();
        s.sequence++;
        sessions.save(s);
        return s.sequence;
    }

    @Transactional
    public Battle transition(UUID id, BattleState expected, BattleState next) {
        var s = sessions.findById(id).orElseThrow();
        if (!s.state.equals(expected.name())) {
            throw new ConflictException("BATTLE_INVALID_STATE", "Battle state changed");
        
        }s.state = next.name();
        s.sequence++;
        sessions.save(s);
        return battle(id);
    }

    public void ready(UUID id, UUID user) {
        var p = participants.findByBattleIdAndUserId(id, user).orElseThrow(() -> new UnauthorizedException("WS_NOT_PARTICIPANT", "Not a battle participant"));
        p.readyAt = Instant.now();
        participants.save(p);
    }

    public boolean bothReady(UUID id) {
        return participants.findByBattleIdOrderByJoinedAt(id).stream().filter(p -> p.readyAt != null).count() == 2;
    }

    public UUID questionVersionId(UUID id, int p) {
        return questions.findByBattleIdAndPosition(id, p).orElseThrow(() -> new NotFoundException("BATTLE_QUESTION_NOT_FOUND", "Battle question not found")).questionVersionId;
    }

    public int questionCount(UUID id) {
        return questions.findByBattleIdOrderByPosition(id).size();
    }

    @Transactional
    public AnswerEffect answer(UUID id, UUID user, UUID q, UUID sub, UUID option, boolean correct, Instant now) {
        var p = participants.findByBattleIdAndUserId(id, user).orElseThrow();
        var b = battle(id);
        if (b.state() != BattleState.RUNNING) {
            return new AnswerEffect(false, "BATTLE_NOT_RUNNING", 0, p.finalScore.doubleValue());
        
        }var question = questions.findByBattleIdAndQuestionVersionId(id, q).orElseThrow(() -> new BadRequestException("BATTLE_QUESTION_INVALID", "Question does not belong to battle"));
        var existing = submissions.findByBattleIdAndSubmissionKey(id, sub);
        if (existing.isPresent()) {
            return new AnswerEffect(false, "DUPLICATE_SUBMISSION", 0, p.finalScore.doubleValue());
        
        }        var awarded = correct ? question.points : BigDecimal.ZERO;
        var s = new BattleSubmissionEntity();
        s.id = UUID.randomUUID();
        s.battleId = id;
        s.participantId = p.id;
        s.questionId = question.id;
        s.submissionKey = sub;
        s.optionId = option;
        s.submittedAt = now;
        s.correct = correct;
        s.awarded = awarded;
        submissions.save(s);
        p.finalScore = p.finalScore.add(awarded);
        participants.save(p);
        return new AnswerEffect(true, "ACCEPTED", awarded.doubleValue(), p.finalScore.doubleValue());
    }

    @Transactional
    public Map<String, Object> finish(UUID id, String reason) {
        var b = transition(id, battle(id).state(), BattleState.FINISHED);
        var ps = participants.findByBattleIdOrderByJoinedAt(id);
        BigDecimal max = ps.stream().map(x -> x.finalScore)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        for (var p : ps) {
            var key = new BattleRatingId();
            key.learnerUserId = p.userId;
            key.skillContext = b.skillContext();
            var r = ratings.findById(key).orElse(null);
            if (r == null) {
                r = new BattleRatingEntity();
                r.learnerUserId = p.userId;
                r.skillContext = b.skillContext();
                r.ratingValue = BigDecimal.valueOf(1000);
            }
            BigDecimal old = r.ratingValue;
            BigDecimal delta = p.finalScore.compareTo(max) == 0
                    ? BigDecimal.valueOf(15)
                    : BigDecimal.valueOf(-15);
            r.ratingValue = old.add(delta).max(BigDecimal.ZERO);
            r.battleCount++;
            r.updatedAt = Instant.now();
            ratings.save(r);
            var h = new BattleHistoryEntity();
            h.id = UUID.randomUUID();
            h.userId = p.userId;
            h.skill = b.skillContext();
            h.battleId = id;
            h.oldRating = old;
            h.newRating = r.ratingValue;
            h.delta = delta;
            h.recordedAt = Instant.now();
            histories.save(h);
        }
        return Map.of("battle_session_id", id, "result_reason", reason);
    }

    public boolean participates(UUID id, UUID u) {
        return participants.findByBattleIdAndUserId(id, u).isPresent();
    }

    public List<Map<String, Object>> ratings(UUID u) {
        return ratings.findByLearnerUserIdOrderBySkillContext(u).stream().map(r -> Map.<String, Object>of("skill_context", r.skillContext, "rating_value", r.ratingValue.doubleValue(), "battle_count", r.battleCount, "updated_at", r.updatedAt)).toList();
    }

    public List<Map<String, Object>> leaderboard(String s, int l) {
        return ratings.findBySkillContextOrderByRatingValueDesc(s).stream().limit(l).map(r -> Map.<String, Object>of("learner_user_id", r.learnerUserId, "rating_value", r.ratingValue.doubleValue(), "battle_count", r.battleCount)).toList();
    }

    public List<Map<String, Object>> history(UUID u) {
        return histories.findByUserIdOrderByRecordedAtDesc(u).stream().map(h -> Map.<String, Object>of("battle_session_id", h.battleId, "skill_context", h.skill, "old_rating", h.oldRating.doubleValue(), "new_rating", h.newRating.doubleValue(), "delta", h.delta.doubleValue(), "recorded_at", h.recordedAt)).toList();
    }

    public Map<String, Object> detail(UUID id, UUID u) {
        if (!participates(id, u)) {
            throw new UnauthorizedException("WS_NOT_PARTICIPANT", "Not a battle participant");
        
        }return Map.of("battle", battle(id));
    }
}
