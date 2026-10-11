package com.skillproof.backend.course.application;

import com.skillproof.backend.course.contract.CourseWorkspaceQuery;
import com.skillproof.backend.feedback.contract.FeedbackSubjectProvider;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CourseFeedbackSubject implements FeedbackSubjectProvider {
    private final CourseWorkspaceQuery courses;

    @org.springframework.beans.factory.annotation.Autowired
    private com.skillproof.backend.course.contract.CourseOfferQuery offers;

    public CourseFeedbackSubject(CourseWorkspaceQuery courses) {
        this.courses = courses;
    }

    public String type() {
        return "COURSE";
    }

    public Subject subject(UUID target, UUID actor) {
        var c = courses.context(target);
        if (actor == null) {
            var offer =
                    offers.offer(com.skillproof.backend.commerce.domain.ProductType.COURSE, target);
            if (!offer.available() || !offer.purchasable())
                throw new org.springframework.security.access.AccessDeniedException(
                        "Course feedback is not public");
        }
        if ("DRAFT".equals(c.status()))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Published course required");
        return new Subject(
                c.organizationId(),
                c.author(),
                actor != null && courses.hasExperience(actor, target),
                c.title());
    }
}
