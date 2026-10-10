package com.skillproof.backend.quiz.application.model;

import java.time.Instant;
import java.util.UUID;

public record BankView(UUID id, UUID organizationId, String title, UUID createdBy, Instant createdAt) {

}
