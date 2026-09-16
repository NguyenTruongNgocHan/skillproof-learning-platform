package com.skillproof.backend.identity.api;

import com.skillproof.backend.identity.application.RegisterUserService;
import com.skillproof.backend.identity.application.ResendEmailVerificationService;
import com.skillproof.backend.identity.application.VerifyEmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class IdentityController {

    private final RegisterUserService
            registerUserService;

    private final VerifyEmailService
            verifyEmailService;

    private final ResendEmailVerificationService
            resendEmailVerificationService;

    public IdentityController(
            RegisterUserService registerUserService,
            VerifyEmailService verifyEmailService,
            ResendEmailVerificationService resendEmailVerificationService
    ) {
        this.registerUserService =
                registerUserService;

        this.verifyEmailService =
                verifyEmailService;

        this.resendEmailVerificationService =
                resendEmailVerificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid
            @RequestBody
            RegisterRequest request
    ) {

        RegisterResponse response =
                registerUserService.register(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse>
    verifyEmail(
            @Valid
            @RequestBody
            VerifyEmailRequest request
    ) {

        return ResponseEntity.ok(
                verifyEmailService.verify(
                        request
                )
        );
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void>
    resendVerification(
            @Valid
            @RequestBody
            ResendVerificationRequest request
    ) {

        resendEmailVerificationService.resend(
                request
        );

        return ResponseEntity
                .accepted()
                .build();
    }
}