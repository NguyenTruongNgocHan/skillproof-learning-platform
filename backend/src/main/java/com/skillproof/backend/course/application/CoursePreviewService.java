package com.skillproof.backend.course.application;

import com.skillproof.backend.commerce.domain.ProductType;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.infrastructure.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CoursePreviewService {
  private final CourseCatalogService catalog;
  private final CourseResourceJpaRepository resources;
  private final CourseModuleJpaRepository modules;
  private final CourseContextRepository versions;
  private final CourseOfferService offers;

  public CoursePreviewService(
      CourseCatalogService catalog,
      CourseResourceJpaRepository resources,
      CourseModuleJpaRepository modules,
      CourseContextRepository versions,
      CourseOfferService offers) {
    this.catalog = catalog;
    this.resources = resources;
    this.modules = modules;
    this.versions = versions;
    this.offers = offers;
  }

  public record Resource(
      UUID id, UUID lessonId, int position, String kind, String title, String body, String url) {}

  public List<Resource> preview(UUID courseId) {
    UUID version = catalog.publicDetail(courseId).versionId();
    return modules.findByVersionIdOrderByPosition(version).stream()
        .flatMap(m -> resources.findByModuleIdOrderByPosition(m.getId()).stream())
        .filter(CourseResourceEntity::isPreview)
        .map(
            r ->
                new Resource(
                    r.getId(),
                    r.getLessonId(),
                    r.getPosition(),
                    r.getKind(),
                    r.getTitle(),
                    r.getBody(),
                    r.getUrl()))
        .toList();
  }

  public void requirePublicResource(UUID id) {
    var r =
        resources
            .findById(id)
            .orElseThrow(() -> new NotFoundException("PREVIEW_NOT_FOUND", "Preview not found"));
    UUID version = modules.findById(r.getModuleId()).orElseThrow().getVersionId();
    var v = versions.findById(version).orElseThrow();
    var offer = offers.offer(ProductType.COURSE, version);
    if (!r.isPreview()
        || !"PUBLISHED".equals(v.getStatus())
        || !offer.available()
        || !offer.purchasable())
      throw new NotFoundException("PREVIEW_NOT_FOUND", "Preview not available");
  }
}
