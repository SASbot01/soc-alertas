package com.blackwolf.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CertificationDTOs {

    @Data
    public static class IssueCertificationRequest {
        private String certificationType;
        private String title;
        private String auditId;
        private String issuedBy;
        private String notes;
    }

    @Data
    public static class CertificationDetailResponse {
        private String id;
        private String companyId;
        private String certificationType;
        private String title;
        private String status;
        private LocalDateTime issuedAt;
        private LocalDateTime expiresAt;
        private String auditId;
        private String issuedBy;
        private String notes;
        private LocalDateTime createdAt;
    }

    @Data
    public static class SocMetricsResponse {
        private long totalAudits;
        private long completedAudits;
        private long totalPentests;
        private long completedPentests;
        private long openVulnerabilities;
        private long fixedVulnerabilities;
        private long activeCertifications;
        private BigDecimal avgRemediationDays;
        private BigDecimal auditDeliveryDays;
        private List<MetricEntry> recentMetrics;
    }

    @Data
    public static class MetricEntry {
        private String name;
        private BigDecimal value;
        private String unit;
        private BigDecimal target;
        private String period;
        private LocalDateTime recordedAt;
    }
}
