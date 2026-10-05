package com.skillproof.backend.learning.api;

import com.skillproof.backend.learning.application.LearningOrderingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/learning")
public class LearningOrderingController {

    private final LearningOrderingService service;

    public LearningOrderingController(LearningOrderingService service) {
        this.service = service;
    }

    public record OrderInput(
        @NotEmpty @Size(max = 1000) List<@NotNull UUID> ids
    ) {}

    @PatchMapping("/versions/{id}/modules/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modules(
        Authentication auth,
        @PathVariable UUID id,
        @Valid @RequestBody OrderInput input
    ) {
        service.modules((UUID) auth.getPrincipal(), id, input.ids());
    }

    @PatchMapping("/modules/{id}/resources/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resources(
        Authentication auth,
        @PathVariable UUID id,
        @Valid @RequestBody OrderInput input
    ) {
        service.resources((UUID) auth.getPrincipal(), id, input.ids());
    }
}
