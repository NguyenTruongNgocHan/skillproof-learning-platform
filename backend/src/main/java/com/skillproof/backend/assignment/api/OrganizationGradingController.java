package com.skillproof.backend.assignment.api;

import com.skillproof.backend.assignment.application.OrganizationGradingService;
import com.skillproof.backend.assignment.infrastructure.persistence.AssignmentEntity;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{org}/grading")
public class OrganizationGradingController {
    private final OrganizationGradingService service;

    public OrganizationGradingController(OrganizationGradingService service) {
        this.service = service;
    }

    @GetMapping
    public Page<AssignmentEntity> list(
            Authentication a, @PathVariable UUID org, @RequestParam(defaultValue = "0") int page) {
        return service.list((UUID) a.getPrincipal(), org, page);
    }

    @GetMapping("/queue")
    public Page<OrganizationGradingService.QueueItem> queue(
            Authentication a,
            @PathVariable UUID org,
            @RequestParam(defaultValue = "SUBMITTED") String status,
            @RequestParam(defaultValue = "0") int page) {
        return service.queue((UUID) a.getPrincipal(), org, status, page);
    }
}
