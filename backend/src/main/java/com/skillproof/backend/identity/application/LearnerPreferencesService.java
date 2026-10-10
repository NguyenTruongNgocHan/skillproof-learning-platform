package com.skillproof.backend.identity.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.identity.application.model.DiscoveryPreferences;
import com.skillproof.backend.identity.domain.AccountStatus;
import com.skillproof.backend.identity.domain.UserRole;
import com.skillproof.backend.identity.infrastructure.LearnerInterestEntity;
import com.skillproof.backend.identity.infrastructure.LearnerInterestRepository;
import com.skillproof.backend.identity.infrastructure.LearnerPreferencesEntity;
import com.skillproof.backend.identity.infrastructure.LearnerPreferencesRepository;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;

@Service
public class LearnerPreferencesService {

    public enum InterestSource {
        USER_SELECTED,
        USER_CONFIRMED_AI
    }

    public record Interest(
            String label,
            InterestSource source) {

    }

    public record State(
            boolean configured,
            boolean personalizationEnabled,
            boolean explorationMode,
            String goalText,
            String experienceLevel,
            List<Interest> interests,
            Instant updatedAt) {

    }

    private final LearnerPreferencesRepository preferences;
    private final LearnerInterestRepository interests;
    private final UserAccountRepository users;

    public LearnerPreferencesService(
            LearnerPreferencesRepository preferences,
            LearnerInterestRepository interests,
            UserAccountRepository users) {

        this.preferences = preferences;
        this.interests = interests;
        this.users = users;
    }

    private void requireLearner(UUID id) {
        var user = users.findById(id)
                .orElseThrow(() -> new AccessDeniedException("Account unavailable"));

        if ((user.getRole() != UserRole.LEARNER && user.getRole() != UserRole.ORGANIZER)
                || user.getStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("Active learner required");
        }
    }

    @Transactional(readOnly = true)
    public State get(UUID id) {
        requireLearner(id);

        var profile = preferences.findById(id);

        List<Interest> learnerInterests = interests
                .findAllByUserIdOrderByCreatedAtAsc(id)
                .stream()
                .map(value -> new Interest(
                value.getLabel(),
                InterestSource.valueOf(value.getSource())))
                .toList();

        if (profile.isEmpty()) {
            return new State(
                    false,
                    true,
                    true,
                    null,
                    null,
                    learnerInterests,
                    null);
        }

        LearnerPreferencesEntity value = profile.get();

        boolean configured
                = value.getGoalText() != null
                || value.getExperienceLevel() != null
                || !learnerInterests.isEmpty()
                || !value.isExplorationMode();

        return new State(
                configured,
                value.isPersonalizationEnabled(),
                value.isExplorationMode(),
                value.getGoalText(),
                value.getExperienceLevel(),
                learnerInterests,
                value.getDiscoveryUpdatedAt());
    }

    @Transactional
    public State save(
            UUID id,
            DiscoveryPreferences input,
            InterestSource source) {

        requireLearner(id);

        LearnerPreferencesEntity profile = preferences
                .findById(id)
                .orElseGet(() -> new LearnerPreferencesEntity(id));

        boolean personalizationEnabled
                = input.personalizationEnabled() == null
                ? profile.isPersonalizationEnabled()
                : input.personalizationEnabled();

        boolean explorationMode
                = input.explorationMode() == null
                ? profile.isExplorationMode()
                : input.explorationMode();

        Instant now = Instant.now();

        profile.updateDiscovery(
                personalizationEnabled,
                explorationMode,
                input.goalText(),
                input.experienceLevel(),
                now);

        preferences.save(profile);

        if (input.interests() != null) {
            replaceInterests(id, input.interests(), source, now);
        }

        return get(id);
    }

    @Transactional
    public void delete(UUID id) {
        requireLearner(id);

        interests.deleteAllByUserId(id);
        preferences.findById(id).ifPresent(profile -> {
            profile.clearDiscovery(Instant.now());
            preferences.save(profile);
        });
    }

    private void replaceInterests(
            UUID userId,
            List<String> rawInterests,
            InterestSource source,
            Instant now) {

        List<String> normalized = normalizeInterests(rawInterests);

        interests.deleteAllByUserId(userId);

        if (normalized.isEmpty()) {
            return;
        }

        List<LearnerInterestEntity> entities = normalized.stream()
                .map(label -> new LearnerInterestEntity(
                UUID.randomUUID(),
                userId,
                label,
                source.name(),
                now))
                .toList();

        interests.saveAll(entities);
    }

    private List<String> normalizeInterests(List<String> values) {
        LinkedHashSet<String> uniqueKeys = new LinkedHashSet<>();
        List<String> result = new ArrayList<>();

        for (String value : values) {
            if (value == null) {
                continue;
            }

            String normalized = value.trim().replaceAll("\\s+", " ");

            if (normalized.isEmpty()) {
                continue;
            }

            String key = normalized.toLowerCase(Locale.ROOT);

            if (uniqueKeys.add(key)) {
                result.add(normalized);
            }
        }

        if (result.size() > 20) {
            throw new IllegalArgumentException(
                    "A learner can select at most 20 interests");
        }

        return List.copyOf(result);
    }
}
