package com.skillproof.backend.identity.infrastructure;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "learner_preferences")
public class LearnerPreferencesEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /*
     * Legacy V7 fields.
     *
     * Kept during the migration so existing learner data is not destroyed.
     * New Discovery flows do not require them.
     */
    @Column(name = "career_goal", length = 100)
    private String careerGoal;

    @Column(name = "target_role", length = 100)
    private String targetRole;

    @Column(name = "skill_level", length = 20)
    private String skillLevel;

    @Column(name = "weekly_goal", length = 20)
    private String weeklyGoal;

    @Column(name = "learning_methods", length = 500)
    private String learningMethods;

    @Column(name = "personalization_enabled", nullable = false)
    private boolean personalizationEnabled = true;

    @Column(name = "exploration_mode", nullable = false)
    private boolean explorationMode = true;

    @Column(name = "goal_text", length = 1000)
    private String goalText;

    @Column(name = "experience_level", length = 30)
    private String experienceLevel;

    @Column(name = "discovery_updated_at")
    private Instant discoveryUpdatedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LearnerPreferencesEntity() {
    }

    public LearnerPreferencesEntity(UUID userId) {
        this.userId = userId;
        this.personalizationEnabled = true;
        this.explorationMode = true;

        Instant now = Instant.now();
        this.discoveryUpdatedAt = now;
        this.updatedAt = now;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isPersonalizationEnabled() {
        return personalizationEnabled;
    }

    public boolean isExplorationMode() {
        return explorationMode;
    }

    public String getGoalText() {
        return goalText;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public Instant getDiscoveryUpdatedAt() {
        return discoveryUpdatedAt;
    }

    public void updateDiscovery(
            boolean personalizationEnabled,
            boolean explorationMode,
            String goalText,
            String experienceLevel,
            Instant updatedAt) {

        this.personalizationEnabled = personalizationEnabled;
        this.explorationMode = explorationMode;
        this.goalText = normalize(goalText);
        this.experienceLevel = normalize(experienceLevel);
        this.discoveryUpdatedAt = updatedAt;
        this.updatedAt = updatedAt;
    }

    public void clearDiscovery(Instant updatedAt) {
        this.personalizationEnabled = true;
        this.explorationMode = true;
        this.goalText = null;
        this.experienceLevel = null;
        this.discoveryUpdatedAt = updatedAt;
        this.updatedAt = updatedAt;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
