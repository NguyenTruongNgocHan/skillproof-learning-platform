package com.skillproof.backend.learning.infrastructure;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_module")
public class LearningModuleEntity {

    @Id
    private UUID id;

    @Column(name = "version_id", nullable = false)
    private UUID versionId;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false, length = 180)
    private String title;

    protected LearningModuleEntity() {
    }

    public LearningModuleEntity(UUID id, UUID versionId, int position, String title) {
        this.id = id;
        this.versionId = versionId;
        this.position = position;
        this.title = title;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVersionId() {
        return versionId;
    }

    public int getPosition() {
        return position;
    }

    public String getTitle() {
        return title;
    }

    public void edit(int p, String t) {
        position = p;
        title = t;
    }

}
