package com.skillproof.backend.learningpath.infrastructure.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_path_item")
public class LearningPathItemEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "path_id", nullable = false)
    private UUID pathId;

    @Column(name = "course_version_id", nullable = false)
    private UUID courseVersionId;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "prerequisite_item_id", nullable = true)
    private UUID prerequisiteItemId;

    protected LearningPathItemEntity() {
    }

    public LearningPathItemEntity(UUID id, UUID pathId, UUID courseVersionId, int position, UUID prerequisiteItemId) {
        this.id = id;
        this.pathId = pathId;
        this.courseVersionId = courseVersionId;
        this.position = position;
        this.prerequisiteItemId = prerequisiteItemId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPathId() {
        return pathId;
    }

    public UUID getCourseVersionId() {
        return courseVersionId;
    }

    public int getPosition() {
        return position;
    }

    public UUID getPrerequisiteItemId() {
        return prerequisiteItemId;
    }

}
