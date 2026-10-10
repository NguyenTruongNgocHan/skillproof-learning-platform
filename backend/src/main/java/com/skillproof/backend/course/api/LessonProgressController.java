package com.skillproof.backend.course.api;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.course.application.LessonProgressService;
import com.skillproof.backend.course.contract.CompletionEvidenceQuery;

@RestController
@RequestMapping("/api/v1/courses/me/enrollments")
public class LessonProgressController {

    private final LessonProgressService service;

    public LessonProgressController(LessonProgressService service) {
        this.service = service;
    }

    @PostMapping("/{id}/lessons/{lesson}/complete")
    public CompletionEvidenceQuery.Evidence complete(Authentication auth, @PathVariable UUID id, @PathVariable UUID lesson) {
        return service.complete((UUID) auth.getPrincipal(), id, lesson);
    }
}
