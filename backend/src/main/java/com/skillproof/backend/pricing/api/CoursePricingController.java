package com.skillproof.backend.pricing.api;

import com.skillproof.backend.pricing.application.CoursePricingService;
import com.skillproof.backend.pricing.infrastructure.*;

import jakarta.validation.*;
import jakarta.validation.constraints.*;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/course-workspace/versions/{version}")
public class CoursePricingController {
    private final CoursePricingService service;

    public CoursePricingController(CoursePricingService service) {
        this.service = service;
    }

    public record PriceInput(
            @NotBlank @Pattern(regexp = "PUBLIC|RESTRICTED") String accessMode,
            @Min(0) long priceVnd,
            @Min(0) long certificationPriceVnd,
            @NotBlank @Size(max = 1000) String reason) {}

    public record PromotionInput(
            @NotBlank @Size(max = 180) String name,
            @Min(1) @Max(90) int percent,
            @NotNull Instant startsAt,
            @NotNull Instant endsAt) {}

    public record LifecycleInput(
            @Pattern(regexp = "ACTIVE|CLOSED|ARCHIVED") @NotBlank String state) {}

    @GetMapping("/commercial")
    public Map<String, Object> get(Authentication a, @PathVariable UUID version) {
        return service.detail((UUID) a.getPrincipal(), version);
    }

    @PutMapping("/commercial")
    public CourseCommercial price(
            Authentication a, @PathVariable UUID version, @Valid @RequestBody PriceInput i) {
        return service.configure(
                (UUID) a.getPrincipal(),
                version,
                i.accessMode(),
                i.priceVnd(),
                i.certificationPriceVnd(),
                i.reason());
    }

    @PutMapping("/lifecycle")
    public CourseCommercial life(
            Authentication a, @PathVariable UUID version, @Valid @RequestBody LifecycleInput i) {
        return service.lifecycle((UUID) a.getPrincipal(), version, i.state());
    }

    @PostMapping("/promotions")
    public CoursePromotion promotion(
            Authentication a, @PathVariable UUID version, @Valid @RequestBody PromotionInput i) {
        return service.promote(
                (UUID) a.getPrincipal(), version, i.name(), i.percent(), i.startsAt(), i.endsAt());
    }

    @DeleteMapping("/promotions/{id}")
    public void cancel(Authentication a, @PathVariable UUID version, @PathVariable UUID id) {
        service.cancel((UUID) a.getPrincipal(), version, id);
    }
}
