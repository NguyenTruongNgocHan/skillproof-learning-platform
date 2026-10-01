package com.skillproof.backend.learning.infrastructure;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_resource")
public class LearningResourceEntity {

    @Id
    private UUID id;

    @Column(name = "module_id", nullable = false)
    private UUID moduleId;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false, length = 12)
    private String kind;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(columnDefinition = "text")
    private String body;

    @Column(length = 1000)
    private String url;

    protected LearningResourceEntity() {
    }

    public LearningResourceEntity(UUID id, UUID moduleId, int position, String kind, String title, String body, String url) {
        this.id = id;
        this.moduleId = moduleId;
        this.position = position;
        this.kind = kind;
        this.title = title;
        this.body = body;
        this.url = url;
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

    public String getKind() {
        return kind;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getUrl() {
        return url;
    }

    public void edit(int p, String k, String t, String b, String u) {
        position = p;
        kind = k;
        title = t;
        body = b;
        url = u;
    }

}
