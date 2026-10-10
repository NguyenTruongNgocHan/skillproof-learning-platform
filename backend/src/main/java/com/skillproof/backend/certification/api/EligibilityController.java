package com.skillproof.backend.certification.api;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.certification.application.EligibilityService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class EligibilityController {

    private final EligibilityService eligibilityService;

    public EligibilityController(EligibilityService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @PostMapping("/certification-programs/{programId}/eligibility/evaluate")
    public EligibilityResponse evaluate(
            Authentication authentication,
            @PathVariable UUID programId,
            @Valid @RequestBody EvaluateEligibilityRequest request
    ) {
        var eligibility = eligibilityService.evaluate(
                authenticatedUserId(authentication),
                programId,
                request.enrollmentId()
        );
        return EligibilityResponse.from(eligibility);
    }

    @GetMapping("/certification-programs/{programId}/eligibility/{learnerId}")
    public EligibilityResponse latest(
            Authentication authentication,
            @PathVariable UUID programId,
            @PathVariable UUID learnerId
    ) {
        return EligibilityResponse.from(
                eligibilityService.latest(
                        authenticatedUserId(authentication),
                        programId,
                        learnerId
                )
        );
    }

    private UUID authenticatedUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }
}
