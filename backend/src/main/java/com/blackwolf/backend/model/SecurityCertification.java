package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "security_certifications")
public class SecurityCertification {
    @Id
    private String id;

    private String companyId;
    private String certificationType;
    private String title;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private String auditId;
    private String issuedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime createdAt;
}
