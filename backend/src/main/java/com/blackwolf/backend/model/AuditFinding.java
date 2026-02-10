package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_findings")
public class AuditFinding {
    @Id
    private String id;

    private String auditId;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String riskLevel;
    private BigDecimal cvssScore;
    private String affectedAsset;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    private String status;
    private LocalDateTime createdAt;
}
