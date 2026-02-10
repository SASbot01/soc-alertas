package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "incident_timeline")
public class IncidentTimeline {
    @Id
    private String id;
    private String incidentId;
    private String action;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String performedBy;
    private LocalDateTime createdAt;
}
