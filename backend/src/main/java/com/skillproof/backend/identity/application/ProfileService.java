package com.skillproof.backend.identity.application;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.identity.application.model.ProfileResponse;
import com.skillproof.backend.identity.application.model.UpdateProfileCommand;
import com.skillproof.backend.identity.domain.UserProfile;
import com.skillproof.backend.identity.infrastructure.UserAccountRepository;
import com.skillproof.backend.identity.infrastructure.UserProfileRepository;
import com.skillproof.backend.media.contract.MediaAvatarQuery;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

  private final UserAccountRepository users;
  private final UserProfileRepository profiles;
  private final MediaAvatarQuery media;

  public ProfileService(
      UserAccountRepository users, UserProfileRepository profiles, MediaAvatarQuery media) {
    this.users = users;
    this.profiles = profiles;
    this.media = media;
  }

  @Transactional(readOnly = true)
  public ProfileResponse get(UUID userId) {
    var user = users.findById(userId).orElseThrow();
    var profile = profiles.findById(userId).orElse(null);
    return toResponse(user, profile);
  }

  @Transactional
  public ProfileResponse update(UUID userId, UpdateProfileCommand request) {
    var user = users.findByIdForUpdate(userId).orElseThrow();
    if (!user.isActive())
      throw new org.springframework.security.access.AccessDeniedException(
          "Active account required");
    try {
      java.time.ZoneId.of(request.timezone());
    } catch (java.time.DateTimeException | NullPointerException invalid) {
      throw new BadRequestException("PROFILE_TIMEZONE", "Use a valid IANA timezone");
    }
    if (request.locale() == null
        || java.util.Locale.forLanguageTag(request.locale()).getLanguage().isBlank())
      throw new BadRequestException("PROFILE_LOCALE", "Use a valid language tag");
    if (request.avatarUrl() != null
        && !request.avatarUrl().isBlank()
        && !request.avatarUrl().startsWith("media:")
        && !request.avatarUrl().matches("https://[^\\s]+"))
      throw new BadRequestException(
          "INVALID_AVATAR", "Use an owned media reference or an HTTPS image URL");
    var profile =
        profiles.findById(userId).orElseGet(() -> UserProfile.create(userId, Instant.now()));
    if (request.avatarUrl() != null && request.avatarUrl().startsWith("media:")) {
      try {
        media.verifyAvatar(userId, UUID.fromString(request.avatarUrl().substring(6)));
      } catch (IllegalArgumentException ex) {
        throw new BadRequestException("INVALID_AVATAR", "Invalid avatar reference");
      }
    }
    user.updateDisplayName(request.displayName());
    profile.update(
        request.headline(),
        request.bio(),
        request.avatarUrl(),
        request.locale(),
        request.timezone(),
        Instant.now());
    profiles.save(profile);
    return toResponse(user, profile);
  }

  private ProfileResponse toResponse(
      com.skillproof.backend.identity.domain.UserAccount user, UserProfile profile) {
    return new ProfileResponse(
        user.getId(),
        user.getEmail(),
        user.getDisplayName(),
        user.getRole(),
        user.getStatus(),
        profile == null ? null : profile.getHeadline(),
        profile == null ? null : profile.getBio(),
        profile == null ? null : profile.getAvatarUrl(),
        profile == null ? "vi-VN" : profile.getLocale(),
        profile == null ? "Asia/Ho_Chi_Minh" : profile.getTimezone(),
        user.getEmailVerifiedAt());
  }
}
