package com.skillproof.backend.library.application;

import com.skillproof.backend.feedback.contract.ExperienceJournal;
import com.skillproof.backend.feedback.contract.FeedbackSubjectProvider;
import com.skillproof.backend.library.infrastructure.persistence.LibraryResourceRepository;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LibraryFeedbackSubject implements FeedbackSubjectProvider {
    @org.springframework.beans.factory.annotation.Autowired
    private com.skillproof.backend.library.contract.LibraryOfferQuery offers;

    private final LibraryResourceRepository resources;
    private final ExperienceJournal receipts;

    public LibraryFeedbackSubject(LibraryResourceRepository resources, ExperienceJournal receipts) {
        this.resources = resources;
        this.receipts = receipts;
    }

    public String type() {
        return "RESOURCE";
    }

    public Subject subject(UUID target, UUID actor) {
        var r = resources.findById(target).orElseThrow();
        if (actor == null) {
            var offer = offers.offer(target);
            if (!offer.available() || !offer.purchasable())
                throw new org.springframework.security.access.AccessDeniedException(
                        "Resource feedback is not public");
        }
        if (!"PUBLISHED".equals(r.getStatus()))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Published resource required");
        return new Subject(
                r.getOrganizationId(),
                r.getAuthorId(),
                actor != null && receipts.hasAccessed(actor, "RESOURCE", target),
                r.getTitle());
    }
}
