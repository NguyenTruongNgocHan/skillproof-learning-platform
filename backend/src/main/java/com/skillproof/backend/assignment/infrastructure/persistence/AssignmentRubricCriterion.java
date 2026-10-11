package com.skillproof.backend.assignment.infrastructure.persistence;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "assignment_rubric_criterion")
public class AssignmentRubricCriterion {
    @Id private UUID id;

    @Column(name = "assignment_id", nullable = false)
    private UUID assignmentId;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false)
    private int weight;

    @Column(nullable = false)
    private int position;

    protected AssignmentRubricCriterion() {}

    public AssignmentRubricCriterion(
            UUID id, UUID assignment, String title, int weight, int position) {
        this.id = id;
        assignmentId = assignment;
        this.title = title;
        this.weight = weight;
        this.position = position;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }

    public String getTitle() {
        return title;
    }

    public int getWeight() {
        return weight;
    }

    public int getPosition() {
        return position;
    }
}
