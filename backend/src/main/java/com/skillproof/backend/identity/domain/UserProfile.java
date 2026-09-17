package com.skillproof.backend.identity.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    private UUID userAccountId;
    private String headline;
    private String bio;
    private String avatarUrl;
    private String locale;
    private String timezone;
    private Instant updatedAt;

    protected UserProfile() {
    }

    public static UserProfile create(UUID userId, Instant now) {
        var profile = new UserProfile();
        profile.userAccountId = userId;
        profile.locale = "vi-VN";
        profile.timezone = "Asia/Ho_Chi_Minh";
        profile.updatedAt = now;
        return profile;
    }

    public void update(String headline, String bio, String avatarUrl, String locale, String timezone, Instant now) {
        this.headline = headline;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.locale = locale;
        this.timezone = timezone;
        this.updatedAt = now;
    }

    public String getHeadline() {
        return headline;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getLocale() {
        return locale;
    }

    public String getTimezone() {
        return timezone;
    }
}
