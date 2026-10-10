package com.skillproof.backend.course.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.infrastructure.CourseContextRepository;
import com.skillproof.backend.course.infrastructure.CourseEntity;
import com.skillproof.backend.course.infrastructure.CourseJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseModuleJpaRepository;
import com.skillproof.backend.course.infrastructure.CourseVersionEntity;

@Service
@Transactional(readOnly = true)
public class CourseCatalogService {

    private final com.skillproof.backend.certification.contract.CertificationOfferingQuery certifications;
    private final CourseModuleJpaRepository modules;
    private final com.skillproof.backend.course.infrastructure.persistence.LessonRepository lessons;
    private final CourseJpaRepository courses;
    private final CourseContextRepository versions;
    private final CourseOfferService offers;

    public CourseCatalogService(CourseJpaRepository courses, CourseContextRepository versions, CourseOfferService offers, CourseModuleJpaRepository modules, com.skillproof.backend.course.infrastructure.persistence.LessonRepository lessons, com.skillproof.backend.certification.contract.CertificationOfferingQuery certifications) {
        this.certifications = certifications;
        this.modules = modules;
        this.lessons = lessons;
        this.courses = courses;
        this.versions = versions;
        this.offers = offers;
    }

    public record Summary(UUID id, UUID versionId, int versionNo, UUID organizationId, String slug, String title, String summary, long priceVnd, long certificationPriceVnd, UUID certificationProgramId) {

    }

    private Summary summary(CourseEntity course, CourseVersionEntity version) {
        return new Summary(course.getId(), version.getId(), version.getVersionNo(), course.getOrganizationId(), course.getSlug(),
                version.getPublishedTitle(), version.getPublishedSummary(), version.getPriceVnd(), version.getCertificationPriceVnd(), certifications.offeringForVersion(version.getId()).map(o -> o.programId()).orElse(null));
    }

    public List<Summary> discover(int page, int size) {
        int bounded = Math.min(Math.max(size, 1), 100);
        return courses.findAll(org.springframework.data.domain.PageRequest.of(Math.max(page, 0), bounded, org.springframework.data.domain.Sort.by("createdAt").descending()))
                .stream().flatMap(course -> versions.findFirstByCourseIdAndStatusOrderByVersionNoDesc(course.getId(), "PUBLISHED").stream()
                .filter(v -> {
                    var offer = offers.offer(ProductType.COURSE, v.getId());
                    return offer.available() && offer.purchasable();
                })
                .map(v -> summary(course, v))).toList();
    }

    public Summary publicDetail(UUID id) {
        var course = courses.findById(id).orElseThrow(() -> new NotFoundException("COURSE_NOT_FOUND", "Published course not found"));
        var version = versions.findFirstByCourseIdAndStatusOrderByVersionNoDesc(id, "PUBLISHED").orElseThrow(() -> new NotFoundException("COURSE_NOT_FOUND", "Published course not found"));
        var offer = offers.offer(ProductType.COURSE, version.getId());
        if (!offer.available() || !offer.purchasable()) {
            throw new NotFoundException("COURSE_NOT_FOUND", "Published course not found");
        }
        return summary(course, version);
    }

    public record LessonSummary(UUID id, int position, String title) {

    }

    public record ModuleSummary(UUID id, int position, String title, List<LessonSummary> lessons) {

    }

    public List<ModuleSummary> syllabus(UUID courseId) {
        UUID version = publicDetail(courseId).versionId();
        return modules.findByVersionIdOrderByPosition(version).stream().map(m -> new ModuleSummary(m.getId(), m.getPosition(), m.getTitle(),
                lessons.findByModuleIdOrderByPosition(m.getId()).stream().map(l -> new LessonSummary(l.getId(), l.getPosition(), l.getTitle())).toList())).toList();
    }

}
