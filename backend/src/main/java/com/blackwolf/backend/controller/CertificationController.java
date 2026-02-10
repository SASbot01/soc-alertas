package com.blackwolf.backend.controller;

import com.blackwolf.backend.dto.CertificationDTOs.*;
import com.blackwolf.backend.model.SecurityCertification;
import com.blackwolf.backend.service.CertificationService;
import com.blackwolf.backend.service.SocMetricsService;
import com.blackwolf.backend.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/certifications")
public class CertificationController {

    @Autowired
    private CertificationService certificationService;

    @Autowired
    private SocMetricsService socMetricsService;

    @Autowired
    private AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<List<SecurityCertification>> list(Authentication auth) {
        return ResponseEntity.ok(certificationService.listByCompany(authUtils.getCompanyId(auth)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificationDetailResponse> getDetail(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(certificationService.getDetail(id, authUtils.getCompanyId(auth)));
    }

    @GetMapping("/eligibility/{auditId}")
    public ResponseEntity<Map<String, Boolean>> checkEligibility(Authentication auth, @PathVariable String auditId) {
        boolean eligible = certificationService.checkEligibility(auditId, authUtils.getCompanyId(auth));
        return ResponseEntity.ok(Map.of("eligible", eligible));
    }

    @PostMapping
    public ResponseEntity<SecurityCertification> issue(Authentication auth, @RequestBody IssueCertificationRequest request) {
        String companyId = authUtils.getCompanyId(auth);
        String performedBy = authUtils.getUser(auth).getEmail();
        return ResponseEntity.ok(certificationService.issue(companyId, request, performedBy));
    }

    @PatchMapping("/{id}/revoke")
    public ResponseEntity<SecurityCertification> revoke(Authentication auth, @PathVariable String id) {
        String companyId = authUtils.getCompanyId(auth);
        String performedBy = authUtils.getUser(auth).getEmail();
        return ResponseEntity.ok(certificationService.revoke(id, companyId, performedBy));
    }

    @GetMapping("/metrics")
    public ResponseEntity<SocMetricsResponse> getMetrics(Authentication auth) {
        return ResponseEntity.ok(socMetricsService.getMetrics(authUtils.getCompanyId(auth)));
    }

    @PostMapping("/metrics")
    public ResponseEntity<Map<String, String>> recordMetric(Authentication auth, @RequestBody Map<String, Object> body) {
        String companyId = authUtils.getCompanyId(auth);
        socMetricsService.recordMetric(
                companyId,
                (String) body.get("metricName"),
                new BigDecimal(body.get("value").toString()),
                (String) body.get("unit"),
                body.get("target") != null ? new BigDecimal(body.get("target").toString()) : null,
                (String) body.get("period")
        );
        return ResponseEntity.ok(Map.of("status", "recorded"));
    }
}
