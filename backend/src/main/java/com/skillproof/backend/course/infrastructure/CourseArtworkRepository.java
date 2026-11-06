package com.skillproof.backend.course.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseArtworkRepository extends JpaRepository<CourseArtwork, UUID> {}
