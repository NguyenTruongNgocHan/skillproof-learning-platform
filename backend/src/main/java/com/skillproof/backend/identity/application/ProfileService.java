package com.skillproof.backend.identity.application;

import com.skillproof.backend.identity.api.ProfileResponse;
import com.skillproof.backend.identity.api.UpdateProfileRequest;
import com.skillproof.backend.identity.domain.UserProfile;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import com.skillproof.backend.identity.infrastructure.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProfileService {
    private final UserAccountRepository users;
    private final UserProfileRepository profiles;

    public ProfileService(UserAccountRepository users, UserProfileRepository profiles) {
        this.users = users;
        this.profiles = profiles;
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(UUID userId) {
        var user = users.findById(userId).orElseThrow();
        var profile = profiles.findById(userId).orElse(null);
        return toResponse(user, profile);
    }

    @Transactional
    public ProfileResponse update(UUID userId, UpdateProfileRequest request) {
        var user = users.findByIdForUpdate(userId).orElseThrow();
        var profile = profiles.findById(userId).orElseGet(() -> UserProfile.create(userId, Instant.now()));
        user.updateDisplayName(request.displayName());
        profile.update(request.headline(), request.bio(), request.avatarUrl(), request.locale(),
                request.timezone(), Instant.now());
        profiles.save(profile);
        return toResponse(user, profile);
    }

    private ProfileResponse toResponse(com.skillproof.backend.identity.domain.UserAccount user, UserProfile profile) {
        return new ProfileResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole(),
                user.getStatus(), profile == null ? null : profile.getHeadline(),
                profile == null ? null : profile.getBio(), profile == null ? null : profile.getAvatarUrl(),
                profile == null ? "vi-VN" : profile.getLocale(),
                profile == null ? "Asia/Ho_Chi_Minh" : profile.getTimezone(), user.getEmailVerifiedAt());
    }
}
