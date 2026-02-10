package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "alert_configurations")
public class AlertConfiguration {
    @Id
    private String id;
    private String companyId;
    private String alertType;
    private String destination;
    private Integer minSeverity;
    private boolean isActive;
    private LocalDateTime createdAt;
}
