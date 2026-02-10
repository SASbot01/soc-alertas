package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "soc_metrics")
public class SocMetric {
    @Id
    private String id;

    private String companyId;
    private String metricName;
    private BigDecimal metricValue;
    private String metricUnit;
    private BigDecimal targetValue;
    private String period;
    private LocalDateTime recordedAt;
}
