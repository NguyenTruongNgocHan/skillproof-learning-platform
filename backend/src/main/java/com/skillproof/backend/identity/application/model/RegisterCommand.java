package com.skillproof.backend.identity.application.model;

public record RegisterCommand(String email, String displayName, String password, com.skillproof.backend.identity.domain.UserRole role) {

}
