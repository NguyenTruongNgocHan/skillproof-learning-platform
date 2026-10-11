package com.skillproof.backend.course.application;

import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.course.contract.CourseWorkspaceQuery;
import com.skillproof.backend.course.infrastructure.*;
import com.skillproof.backend.media.contract.ArtworkFiles;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CourseArtworkService {
    private final CourseArtworkRepository artwork;
    private final CourseWorkspaceQuery courses;
    private final CourseCatalogService catalog;
    private final ArtworkFiles files;

    public CourseArtworkService(
            CourseArtworkRepository artwork,
            CourseWorkspaceQuery courses,
            CourseCatalogService catalog,
            ArtworkFiles files) {
        this.artwork = artwork;
        this.courses = courses;
        this.catalog = catalog;
        this.files = files;
    }

    public Map<String, Object> read(UUID actor, UUID id) {
        courses.requireCourseAuthor(actor, id);
        Map<String, Object> r = new LinkedHashMap<>();
        artwork.findById(id)
                .ifPresent(
                        b -> {
                            r.put("coverMediaId", b.getCoverMediaId());
                            r.put("updatedAt", b.getUpdatedAt());
                        });
        return r;
    }

    @Transactional
    public Map<String, Object> update(UUID actor, UUID id, UUID cover) {
        courses.requireCourseAuthor(actor, id);
        courses.lockCourse(id);
        if (cover != null) files.requireImage(cover, "COURSE_COVER", id);
        var b = artwork.findById(id).orElseGet(() -> new CourseArtwork(id));
        b.update(null, cover);
        artwork.save(b);
        return read(actor, id);
    }

    public ArtworkFiles.Image image(UUID id) {
        catalog.publicDetail(id);
        UUID media =
                artwork.findById(id)
                        .map(CourseArtwork::getCoverMediaId)
                        .orElseThrow(
                                () -> new NotFoundException("IMAGE_NOT_FOUND", "No course cover"));
        return files.image(media);
    }
}
