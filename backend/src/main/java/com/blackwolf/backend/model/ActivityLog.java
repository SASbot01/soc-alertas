package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "activity_log")
public class ActivityLog {
    @Id
    private String id;

    private String companyId;
    private String entityType;
    private String entityId;
    private String action;
    private String performedBy;

    @Column(columnDefinition = "TEXT")
    private String details;

    private LocalDateTime createdAt;
}
