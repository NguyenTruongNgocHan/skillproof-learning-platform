package com.skillproof.backend.course.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseArtworkRepository extends JpaRepository<CourseArtwork, UUID> {}
