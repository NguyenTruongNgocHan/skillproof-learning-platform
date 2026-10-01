package com.skillproof.backend.realtime.application;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.common.exception.UnauthorizedException;
import com.skillproof.backend.quiz.contract.BattleQuestionQuery;
import com.skillproof.backend.realtime.domain.BattleState;

@Service
public class BattleCoordinator {

    public interface Events {

        void user(UUID user, String type, UUID battle, long sequence, Object payload);

        void battle(UUID battle, String type, long sequence, Object payload);
    }
    private final StringRedisTemplate redis;
    private final BattleRepository repo;
    private final BattleQuestionQuery questions;
    private final int count;
    private final int seconds;
    private final ScheduledExecutorService timers = Executors.newScheduledThreadPool(2);
    private volatile Events events;

    public BattleCoordinator(StringRedisTemplate redis, BattleRepository repo, BattleQuestionQuery questions, @Value("${skillproof.realtime.question-count:5}") int count, @Value("${skillproof.realtime.question-seconds:20}") int seconds) {
        this.redis = redis;
        this.repo = repo;
        this.questions = questions;
        this.count = count;
        this.seconds = seconds;
    }

    public void events(Events events) {
        this.events = events;
    }

    public void join(UUID user, String skill) {
        String q = "rt:queue:" + skill;
        String existing = redis.opsForValue().get("rt:user-queue:" + user);
        if (existing != null) {
            throw new ConflictException("QUEUE_CONFLICT", "Already queued");
        }
        String opponent = redis.opsForList().leftPop(q);
        if (opponent == null) {
            redis.opsForList().rightPush(q, user.toString());
            redis.opsForValue().set("rt:user-queue:" + user, skill, Duration.ofMinutes(10));
            emitUser(user, "queue.joined", null, 0, Map.of("skillContext", skill, "joinedAt", Instant.now()));
            return;
        }
        UUID other = UUID.fromString(opponent);
        redis.delete("rt:user-queue:" + other);
        var qs = questions.selectPublishedQuestions(skill, count);
        if (qs.isEmpty()) {
            redis.opsForList().rightPush(q, other.toString());
            throw new ConflictException("BATTLE_QUESTION_POOL_EMPTY", "No published questions available for battle");
        }
        var b = repo.create(skill, other, user, qs.stream().map(seed -> new BattleRepository.QuestionSeed(seed.questionVersionId())).toList());
        emitUser(other, "match.found", b.id(), b.sequence(), Map.of("opponentId", user, "skillContext", skill));
        emitUser(user, "match.found", b.id(), b.sequence(), Map.of("opponentId", other, "skillContext", skill));
        snapshot(b);
    }

    public void leave(UUID user) {
        String skill = redis.opsForValue().get("rt:user-queue:" + user);
        if (skill == null) {
            return;
        }
        redis.opsForList().remove("rt:queue:" + skill, 1, user.toString());
        redis.delete("rt:user-queue:" + user);
        emitUser(user, "queue.left", null, 0, Map.of("reason", "CLIENT_LEFT"));
    }

    public String createPrivate(UUID user, String skill) {
        String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        redis.opsForHash().put("rt:room:" + code, "owner", user.toString());
        redis.opsForHash().put("rt:room:" + code, "skill", skill);
        redis.expire("rt:room:" + code, Duration.ofMinutes(10));
        return code;
    }

    public void joinPrivate(UUID user, String code) {
        String key = "rt:room:" + code.toUpperCase();
        Object owner = redis.opsForHash().get(key, "owner");
        Object skill = redis.opsForHash().get(key, "skill");
        if (owner == null || skill == null) {
            throw new NotFoundException("ROOM_NOT_FOUND", "Private room not found or expired");
        }
        UUID other = UUID.fromString(owner.toString());
        if (other.equals(user)) {
            throw new BadRequestException("ROOM_OWNER_CANNOT_JOIN", "Room owner is already waiting");
        }
        var qs = questions.selectPublishedQuestions(skill.toString(), count);
        if (qs.isEmpty()) {
            throw new ConflictException("BATTLE_QUESTION_POOL_EMPTY", "No published questions available for battle");
        }
        redis.delete(key);
        var b = repo.create(skill.toString(), other, user, qs.stream().map(q -> new BattleRepository.QuestionSeed(q.questionVersionId())).toList());
        emitUser(other, "match.found", b.id(), b.sequence(), Map.of("opponentId", user, "skillContext", skill, "privateRoom", code));
        emitUser(user, "match.found", b.id(), b.sequence(), Map.of("opponentId", other, "skillContext", skill, "privateRoom", code));
        snapshot(b);
    }

    public void ready(UUID user, UUID battle) {
        var b = repo.get(battle);
        participant(b, user);
        repo.ready(battle, user);
        if (repo.bothReady(battle) && b.state() == BattleState.MATCHED) {
            var ready = repo.transition(battle, BattleState.MATCHED, BattleState.READY);
            state(ready, BattleState.MATCHED);
            var countdown = repo.transition(battle, BattleState.READY, BattleState.COUNTDOWN);
            state(countdown, BattleState.READY);
            timers.schedule(() -> start(battle), 3, TimeUnit.SECONDS);
        } else {
            snapshot(repo.get(battle));
        }
    }

    private void start(UUID id) {
        try {
            var running = repo.transition(id, BattleState.COUNTDOWN, BattleState.RUNNING);
            state(running, BattleState.COUNTDOWN);
            openQuestion(id, 1);
        } catch (RuntimeException ignored) {
        }
    }

    private void openQuestion(UUID id, int pos) {
        var b = repo.get(id);
        if (b.state() != BattleState.RUNNING) {
            return;
        }
        if (pos > repo.questionCount(id)) {
            finish(id, "QUESTIONS_COMPLETED");
            return;
        }
        UUID questionVersionId = repo.questionVersionId(id, pos);
        var q = questions.findPublishedQuestion(questionVersionId)
                .orElseThrow(() -> new NotFoundException("BATTLE_QUESTION_NOT_FOUND", "Battle question is no longer available"));
        Instant closes = Instant.now().plusSeconds(seconds);
        redis.opsForHash().put("rt:battle:" + id, "position", Integer.toString(pos));
        redis.opsForHash().put("rt:battle:" + id, "closesAt", closes.toString());
        emitBattle(id, "battle.question.opened", repo.nextSequence(id), Map.of("question", q, "closesAt", closes));
        timers.schedule(() -> advanceIfCurrent(id, pos), seconds, TimeUnit.SECONDS);
    }

    private void advanceIfCurrent(UUID id, int pos) {
        Object current = redis.opsForHash().get("rt:battle:" + id, "position");
        if (current != null && Integer.parseInt(current.toString()) == pos) {
            openQuestion(id, pos + 1);
        }
    }

    public void answer(UUID user, UUID battle, UUID question, UUID submission, UUID option) {
        var b = repo.get(battle);
        participant(b, user);
        Object position = redis.opsForHash().get("rt:battle:" + battle, "position");
        Object closes = redis.opsForHash().get("rt:battle:" + battle, "closesAt");
        if (position == null) {
            emitUser(user, "battle.answer.rejected", battle, b.sequence(), Map.of("code", "QUESTION_CONTEXT_MISMATCH", "submissionId", submission));
            return;
        }
        UUID currentQuestionVersionId = repo.questionVersionId(battle, Integer.parseInt(position.toString()));
        if (!question.equals(currentQuestionVersionId)) {
            emitUser(user, "battle.answer.rejected", battle, b.sequence(), Map.of("code", "QUESTION_CONTEXT_MISMATCH", "submissionId", submission));
            return;
        }
        if (closes == null || Instant.now().isAfter(Instant.parse(closes.toString()))) {
            emitUser(user, "battle.answer.rejected", battle, b.sequence(), Map.of("code", "ANSWER_TIMEOUT", "submissionId", submission));
            return;
        }
        boolean correct = questions.isCorrectOption(question, option);
        var effect = repo.answer(battle, user, question, submission, option, correct, Instant.now());
        if (!effect.accepted()) {
            emitUser(user, "battle.answer.rejected", battle, b.sequence(), Map.of("code", effect.code(), "submissionId", submission));
            return;
        }
        long seq = repo.nextSequence(battle);
        emitUser(user, "battle.answer.accepted", battle, seq, Map.of("submissionId", submission));
        emitBattle(battle, "battle.score.updated", seq, Map.of("userId", user, "score", effect.score()));
    }

    public void snapshot(UUID user, UUID battle) {
        participant(repo.get(battle), user);
        snapshot(repo.get(battle));
    }

    private void snapshot(BattleRepository.Battle b) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("battleId", b.id());
        p.put("state", b.state());
        p.put("sequence", b.sequence());
        p.put("serverTime", Instant.now());
        p.put("participants", b.participants());
        Object pos = redis.opsForHash().get("rt:battle:" + b.id(), "position");
        if (pos != null && b.state() == BattleState.RUNNING) {
            UUID questionVersionId = repo.questionVersionId(b.id(), Integer.parseInt(pos.toString()));
            questions.findPublishedQuestion(questionVersionId).ifPresent(question -> p.put("currentQuestion", question));
        }
        emitBattle(b.id(), "battle.snapshot", b.sequence(), p);
    }

    private void finish(UUID id, String reason) {
        var result = repo.finish(id, reason);
        var b = repo.get(id);
        redis.delete("rt:battle:" + id);
        emitBattle(id, "battle.finished", b.sequence(), result);
    }

    private void participant(BattleRepository.Battle b, UUID user) {
        if (b.participants().stream().noneMatch(p -> p.userId().equals(user))) {
            throw new UnauthorizedException("WS_NOT_PARTICIPANT", "Not a battle participant");
        }
    }

    private void state(BattleRepository.Battle b, BattleState from) {
        emitBattle(b.id(), "battle.state.changed", b.sequence(), Map.of("from", from, "to", b.state(), "effectiveAt", Instant.now()));
    }

    private void emitUser(UUID u, String t, UUID b, long s, Object p) {
        if (events != null) {
            events.user(u, t, b, s, p);
        }
    }

    private void emitBattle(UUID b, String t, long s, Object p) {
        if (events != null) {
            events.battle(b, t, s, p);
        }
    }
}
