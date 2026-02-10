package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "incidents")
public class Incident {
    @Id
    private String id;
    private String companyId;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String severity;
    private String status;
    private String assignedTo;
    private String sourceThreatId;
    private LocalDateTime slaDeadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
