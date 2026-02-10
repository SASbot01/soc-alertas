package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "security_audits")
public class SecurityAudit {
    @Id
    private String id;

    private String companyId;
    private String title;
    private String auditType;
    private String status;

    @Column(columnDefinition = "TEXT")
    private String scope;

    @Column(columnDefinition = "TEXT")
    private String methodology;

    private String leadAuditor;
    private LocalDate startDate;
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String executiveSummary;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
