package com.skillproof.backend.identity.application;

import com.skillproof.backend.identity.api.AuthResponse;

public record IssuedSession(AuthResponse response, String refreshToken) {

}
