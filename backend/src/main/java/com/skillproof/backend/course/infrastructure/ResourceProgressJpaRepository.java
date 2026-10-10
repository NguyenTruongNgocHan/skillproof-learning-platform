package com.skillproof.backend.course.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceProgressJpaRepository extends JpaRepository<ResourceProgressEntity, ResourceProgressId> {

    List<ResourceProgressEntity> findByEnrollmentId(UUID enrollmentId);

    @org.springframework.data.jpa.repository.Query("select count(p) from ResourceProgressEntity p, CourseResourceEntity r where p.resourceId = r.id and p.enrollmentId = :enrollmentId and r.requiredForCompletion = true")
    long countByEnrollmentId(UUID enrollmentId);
}
