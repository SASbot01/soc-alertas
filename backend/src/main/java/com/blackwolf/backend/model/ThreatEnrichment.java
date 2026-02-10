package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "threat_enrichments")
public class ThreatEnrichment {
    @Id
    private String ip;
    private Integer abuseConfidenceScore;
    private String countryCode;
    private String isp;
    private String domain;
    private boolean isTor;
    private boolean isVpn;
    private Integer totalReports;
    private LocalDateTime lastReportedAt;
    private LocalDateTime enrichedAt;
}
