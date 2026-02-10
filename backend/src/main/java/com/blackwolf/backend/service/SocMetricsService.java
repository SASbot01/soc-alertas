package com.blackwolf.backend.service;

import com.blackwolf.backend.dto.CertificationDTOs.*;
import com.blackwolf.backend.model.SocMetric;
import com.blackwolf.backend.model.Vulnerability;
import com.blackwolf.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SocMetricsService {

    @Autowired
    private SecurityAuditRepository auditRepository;

    @Autowired
    private PenetrationTestRepository pentestRepository;

    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;

    @Autowired
    private SecurityCertificationRepository certificationRepository;

    @Autowired
    private SocMetricRepository socMetricRepository;

    public SocMetricsResponse getMetrics(String companyId) {
        SocMetricsResponse response = new SocMetricsResponse();

        response.setTotalAudits(auditRepository.findByCompanyId(companyId).size());
        response.setCompletedAudits(auditRepository.countByCompanyIdAndStatus(companyId, "DELIVERED"));
        response.setTotalPentests(pentestRepository.findByCompanyId(companyId).size());
        response.setCompletedPentests(pentestRepository.findByCompanyIdAndStatus(companyId, "DELIVERED").size());

        List<Vulnerability> vulns = vulnerabilityRepository.findByCompanyId(companyId);
        response.setOpenVulnerabilities(vulns.stream()
                .filter(v -> !"FIXED".equals(v.getStatus()) && !"VERIFIED".equals(v.getStatus()))
                .count());
        response.setFixedVulnerabilities(vulns.stream()
                .filter(v -> "FIXED".equals(v.getStatus()) || "VERIFIED".equals(v.getStatus()))
                .count());

        response.setActiveCertifications(
                certificationRepository.countByCompanyIdAndExpiresAtAfter(companyId, LocalDateTime.now()));

        // Calculate average remediation days for fixed/verified vulnerabilities
        List<Vulnerability> resolved = vulns.stream()
                .filter(v -> ("FIXED".equals(v.getStatus()) || "VERIFIED".equals(v.getStatus())) && v.getUpdatedAt() != null && v.getDetectedAt() != null)
                .collect(Collectors.toList());

        if (!resolved.isEmpty()) {
            double avgDays = resolved.stream()
                    .mapToLong(v -> ChronoUnit.DAYS.between(v.getDetectedAt(), v.getUpdatedAt()))
                    .average()
                    .orElse(0);
            response.setAvgRemediationDays(BigDecimal.valueOf(avgDays).setScale(1, RoundingMode.HALF_UP));
        } else {
            response.setAvgRemediationDays(BigDecimal.ZERO);
        }

        response.setAuditDeliveryDays(BigDecimal.ZERO);

        // Recent custom metrics
        List<SocMetric> recentMetrics = socMetricRepository.findByCompanyId(companyId);
        response.setRecentMetrics(recentMetrics.stream().map(m -> {
            MetricEntry entry = new MetricEntry();
            entry.setName(m.getMetricName());
            entry.setValue(m.getMetricValue());
            entry.setUnit(m.getMetricUnit());
            entry.setTarget(m.getTargetValue());
            entry.setPeriod(m.getPeriod());
            entry.setRecordedAt(m.getRecordedAt());
            return entry;
        }).collect(Collectors.toList()));

        return response;
    }

    public SocMetric recordMetric(String companyId, String metricName, BigDecimal value, String unit, BigDecimal target, String period) {
        SocMetric metric = new SocMetric();
        metric.setId(UUID.randomUUID().toString());
        metric.setCompanyId(companyId);
        metric.setMetricName(metricName);
        metric.setMetricValue(value);
        metric.setMetricUnit(unit);
        metric.setTargetValue(target);
        metric.setPeriod(period);
        metric.setRecordedAt(LocalDateTime.now());
        return socMetricRepository.save(metric);
    }
}
