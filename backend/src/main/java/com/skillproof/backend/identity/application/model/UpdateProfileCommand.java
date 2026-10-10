package com.skillproof.backend.identity.application.model;

public record UpdateProfileCommand(String displayName, String headline, String bio, String avatarUrl, String locale, String timezone) {

}
