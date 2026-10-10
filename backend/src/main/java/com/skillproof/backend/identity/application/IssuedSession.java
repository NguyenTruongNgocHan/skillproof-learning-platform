package com.skillproof.backend.identity.application;

import com.skillproof.backend.identity.application.model.AuthResponse;

public record IssuedSession(AuthResponse response, String refreshToken) {

}
