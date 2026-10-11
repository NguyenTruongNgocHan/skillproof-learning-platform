package com.skillproof.backend.operations.api;

import com.skillproof.backend.operations.application.WorkspaceActivityService;
import com.skillproof.backend.operations.infrastructure.*;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/workspace")
public class WorkspaceActivityController {
    private final WorkspaceActivityService service;

    public WorkspaceActivityController(WorkspaceActivityService service) {
        this.service = service;
    }

    @GetMapping("/organizations/{org}/audit")
    public Page<AuditEntry> audit(
            Authentication a, @PathVariable UUID org, @RequestParam(defaultValue = "0") int page) {
        return service.audit((UUID) a.getPrincipal(), org, page);
    }

    @GetMapping("/notifications")
    public Page<WorkspaceNotice> notices(
            Authentication a, @RequestParam(defaultValue = "0") int page) {
        return service.notices((UUID) a.getPrincipal(), page);
    }

    @GetMapping("/notifications/unread")
    public Map<String, Long> unread(Authentication a) {
        return Map.of("count", service.unread((UUID) a.getPrincipal()));
    }

    @PostMapping("/notifications/{id}/read")
    public void read(Authentication a, @PathVariable UUID id) {
        service.read((UUID) a.getPrincipal(), id);
    }
}
