package com.skillproof.backend.realtime.infrastructure;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "battle_session")
class BattleSessionEntity {

    @Id
    UUID id;
    @Column(name = "skill_context")
    String skill;
    String state;
    @Column(name = "sequence_no")
    long sequence;
    @Column(name = "created_at")
    Instant created;
    @Version
    long version;
}

@Entity
@Table(name = "battle_participant")
class BattleParticipantEntity {

    @Id
    UUID id;
    @Column(name = "battle_session_id")
    UUID battleId;
    @Column(name = "learner_user_id")
    UUID userId;
    @Column(name = "rating_snapshot", precision = 10, scale = 2)
    BigDecimal ratingSnapshot;
    @Column(name = "final_score", precision = 10, scale = 2)
    BigDecimal finalScore;
    Instant readyAt;
    Instant joinedAt;
}

@Entity
@Table(name = "battle_question")
class BattleQuestionEntity {

    @Id
    UUID id;
    @Column(name = "battle_session_id")
    UUID battleId;
    @Column(name = "question_version_id")
    UUID questionVersionId;
    int position;
    @Column(precision = 10, scale = 2)
    BigDecimal points;
}

@Entity
@Table(name = "battle_rating")
@IdClass(BattleRatingId.class)
class BattleRatingEntity {

    @Id
    UUID learnerUserId;
    @Id
    String skillContext;
    @Column(name = "rating_value", precision = 10, scale = 2)
    BigDecimal ratingValue;
    int battleCount;
    Instant updatedAt;
}

class BattleRatingId implements java.io.Serializable {

    UUID learnerUserId;
    String skillContext;

    public boolean equals(Object o) {
        return o instanceof BattleRatingId x && Objects.equals(learnerUserId, x.learnerUserId) && Objects.equals(skillContext, x.skillContext);
    }

    public int hashCode() {
        return Objects.hash(learnerUserId, skillContext);
    }
}

interface BattleSessionJpaRepository extends JpaRepository<BattleSessionEntity, UUID> {
}

interface BattleParticipantJpaRepository extends JpaRepository<BattleParticipantEntity, UUID> {

    List<BattleParticipantEntity> findByBattleIdOrderByJoinedAt(UUID id);

    Optional<BattleParticipantEntity> findByBattleIdAndUserId(UUID b, UUID u);
}

interface BattleQuestionJpaRepository extends JpaRepository<BattleQuestionEntity, UUID> {

    List<BattleQuestionEntity> findByBattleIdOrderByPosition(UUID id);

    Optional<BattleQuestionEntity> findByBattleIdAndPosition(UUID b, int p);

    Optional<BattleQuestionEntity> findByBattleIdAndQuestionVersionId(UUID b, UUID q);
}

interface BattleRatingJpaRepository extends JpaRepository<BattleRatingEntity, BattleRatingId> {

    List<BattleRatingEntity> findByLearnerUserIdOrderBySkillContext(UUID id);

    List<BattleRatingEntity> findBySkillContextOrderByRatingValueDesc(String s);
}

@Entity
@Table(name = "battle_answer_submission")
class BattleSubmissionEntity {

    @Id
    UUID id;
    @Column(name = "battle_session_id")
    UUID battleId;
    @Column(name = "participant_id")
    UUID participantId;
    @Column(name = "battle_question_id")
    UUID questionId;
    @Column(name = "submission_key", unique = true)
    UUID submissionKey;
    @Column(name = "selected_option_id")
    UUID optionId;
    @Column(name = "submitted_at")
    Instant submittedAt;
    @Column(name = "is_correct")
    boolean correct;
    @Column(name = "awarded_points", precision = 10, scale = 2)
    BigDecimal awarded;
}

@Entity
@Table(name = "battle_rating_history")
class BattleHistoryEntity {

    @Id
    UUID id;
    @Column(name = "learner_user_id")
    UUID userId;
    @Column(name = "skill_context")
    String skill;
    @Column(name = "battle_session_id")
    UUID battleId;
    @Column(name = "old_rating", precision = 10, scale = 2)
    BigDecimal oldRating;
    @Column(name = "new_rating", precision = 10, scale = 2)
    BigDecimal newRating;
    @Column(precision = 10, scale = 2)
    BigDecimal delta;
    Instant recordedAt;
}

interface BattleSubmissionJpaRepository extends JpaRepository<BattleSubmissionEntity, UUID> {

    Optional<BattleSubmissionEntity> findByBattleIdAndSubmissionKey(UUID battle, UUID key);
}

interface BattleHistoryJpaRepository extends JpaRepository<BattleHistoryEntity, UUID> {

    List<BattleHistoryEntity> findByUserIdOrderByRecordedAtDesc(UUID userId);
}
