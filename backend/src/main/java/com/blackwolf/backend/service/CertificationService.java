package com.blackwolf.backend.service;

import com.blackwolf.backend.dto.CertificationDTOs.*;
import com.blackwolf.backend.model.SecurityAudit;
import com.blackwolf.backend.model.SecurityCertification;
import com.blackwolf.backend.repository.AuditFindingRepository;
import com.blackwolf.backend.repository.SecurityAuditRepository;
import com.blackwolf.backend.repository.SecurityCertificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CertificationService {

    @Autowired
    private SecurityCertificationRepository certRepository;

    @Autowired
    private SecurityAuditRepository auditRepository;

    @Autowired
    private AuditFindingRepository findingRepository;

    @Autowired
    private ActivityLogService activityLogService;

    public List<SecurityCertification> listByCompany(String companyId) {
        return certRepository.findByCompanyId(companyId);
    }

    public CertificationDetailResponse getDetail(String certId, String companyId) {
        SecurityCertification cert = certRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));
        if (!cert.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Access denied");
        }

        CertificationDetailResponse response = new CertificationDetailResponse();
        response.setId(cert.getId());
        response.setCompanyId(cert.getCompanyId());
        response.setCertificationType(cert.getCertificationType());
        response.setTitle(cert.getTitle());
        response.setStatus(cert.getStatus());
        response.setIssuedAt(cert.getIssuedAt());
        response.setExpiresAt(cert.getExpiresAt());
        response.setAuditId(cert.getAuditId());
        response.setIssuedBy(cert.getIssuedBy());
        response.setNotes(cert.getNotes());
        response.setCreatedAt(cert.getCreatedAt());
        return response;
    }

    public boolean checkEligibility(String auditId, String companyId) {
        SecurityAudit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new RuntimeException("Audit not found"));
        if (!audit.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Access denied");
        }

        if (!"DELIVERED".equals(audit.getStatus())) {
            return false;
        }

        long openFindings = findingRepository.countByAuditIdAndStatus(auditId, "OPEN");
        return openFindings == 0;
    }

    @Transactional
    public SecurityCertification issue(String companyId, IssueCertificationRequest request, String performedBy) {
        if (request.getAuditId() != null) {
            boolean eligible = checkEligibility(request.getAuditId(), companyId);
            if (!eligible) {
                throw new RuntimeException("Company is not eligible for certification. Audit must be DELIVERED with no open findings.");
            }
        }

        SecurityCertification cert = new SecurityCertification();
        cert.setId(UUID.randomUUID().toString());
        cert.setCompanyId(companyId);
        cert.setCertificationType(request.getCertificationType());
        cert.setTitle(request.getTitle());
        cert.setStatus("ACTIVE");
        cert.setIssuedAt(LocalDateTime.now());
        cert.setExpiresAt(LocalDateTime.now().plusMonths(12));
        cert.setAuditId(request.getAuditId());
        cert.setIssuedBy(request.getIssuedBy());
        cert.setNotes(request.getNotes());
        cert.setCreatedAt(LocalDateTime.now());

        cert = certRepository.save(cert);

        activityLogService.log(companyId, "CERTIFICATION", cert.getId(), "ISSUED", performedBy,
                "Certification issued: " + cert.getTitle() + " [" + cert.getCertificationType() + "]");

        return cert;
    }

    @Transactional
    public SecurityCertification revoke(String certId, String companyId, String performedBy) {
        SecurityCertification cert = certRepository.findById(certId)
                .orElseThrow(() -> new RuntimeException("Certification not found"));
        if (!cert.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Access denied");
        }

        cert.setStatus("REVOKED");
        cert = certRepository.save(cert);

        activityLogService.log(companyId, "CERTIFICATION", cert.getId(), "REVOKED", performedBy,
                "Certification revoked: " + cert.getTitle());

        return cert;
    }
}
