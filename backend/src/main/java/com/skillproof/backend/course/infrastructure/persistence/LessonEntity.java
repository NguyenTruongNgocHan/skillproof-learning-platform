package com.skillproof.backend.course.infrastructure.persistence;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_lesson")
public class LessonEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "module_id", nullable = false)
    private UUID moduleId;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "text")
    private String body;

    protected LessonEntity() {
    }

    public LessonEntity(UUID id, UUID moduleId, int position, String title, String body) {
        this.id = id;
        this.moduleId = moduleId;
        this.position = position;
        this.title = title;
        this.body = body;
    }

    public UUID getId() {
        return id;
    }

    public UUID getModuleId() {
        return moduleId;
    }

    public int getPosition() {
        return position;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public void edit(int position, String title, String body) {
        this.position = position;
        this.title = title;
        this.body = body;
    }
}
