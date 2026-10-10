package com.skillproof.backend.identity.application.model;

public record ResetPasswordCommand(String token, String newPassword) {

}
