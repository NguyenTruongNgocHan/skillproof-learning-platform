package com.skillproof.backend.identity.application;

import java.time.Instant;

public record IssuedPasswordReset(String rawToken, Instant expiresAt) {

}
