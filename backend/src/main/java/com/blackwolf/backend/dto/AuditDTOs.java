package com.blackwolf.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AuditDTOs {

    @Data
    public static class CreateAuditRequest {
        private String title;
        private String auditType;
        private String scope;
        private String methodology;
        private String leadAuditor;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    public static class UpdateAuditStatusRequest {
        private String status;
        private String executiveSummary;
    }

    @Data
    public static class AuditDetailResponse {
        private String id;
        private String companyId;
        private String title;
        private String auditType;
        private String status;
        private String scope;
        private String methodology;
        private String leadAuditor;
        private LocalDate startDate;
        private LocalDate endDate;
        private String executiveSummary;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<FindingSummary> findings;
    }

    @Data
    public static class CreateFindingRequest {
        private String title;
        private String description;
        private String riskLevel;
        private BigDecimal cvssScore;
        private String affectedAsset;
        private String recommendation;
    }

    @Data
    public static class FindingSummary {
        private String id;
        private String title;
        private String riskLevel;
        private BigDecimal cvssScore;
        private String affectedAsset;
        private String status;
        private LocalDateTime createdAt;
    }
}
