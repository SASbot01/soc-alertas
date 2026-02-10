package com.blackwolf.backend.service;

import com.blackwolf.backend.dto.AuditDTOs.*;
import com.blackwolf.backend.model.AuditFinding;
import com.blackwolf.backend.model.SecurityAudit;
import com.blackwolf.backend.repository.AuditFindingRepository;
import com.blackwolf.backend.repository.SecurityAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private static final List<String> AUDIT_LIFECYCLE = List.of(
            "SCOPING", "SCANNING", "TESTING", "REPORTING", "DELIVERED"
    );

    @Autowired
    private SecurityAuditRepository auditRepository;

    @Autowired
    private AuditFindingRepository findingRepository;

    @Autowired
    private ActivityLogService activityLogService;

    public List<SecurityAudit> listByCompany(String companyId) {
        return auditRepository.findByCompanyId(companyId);
    }

    public AuditDetailResponse getDetail(String auditId, String companyId) {
        SecurityAudit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new RuntimeException("Audit not found"));
        if (!audit.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Access denied");
        }

        List<AuditFinding> findings = findingRepository.findByAuditId(auditId);

        AuditDetailResponse response = new AuditDetailResponse();
        response.setId(audit.getId());
        response.setCompanyId(audit.getCompanyId());
        response.setTitle(audit.getTitle());
        response.setAuditType(audit.getAuditType());
        response.setStatus(audit.getStatus());
        response.setScope(audit.getScope());
        response.setMethodology(audit.getMethodology());
        response.setLeadAuditor(audit.getLeadAuditor());
        response.setStartDate(audit.getStartDate());
        response.setEndDate(audit.getEndDate());
        response.setExecutiveSummary(audit.getExecutiveSummary());
        response.setCreatedAt(audit.getCreatedAt());
        response.setUpdatedAt(audit.getUpdatedAt());
        response.setFindings(findings.stream().map(this::toFindingSummary).collect(Collectors.toList()));

        return response;
    }

    @Transactional
    public SecurityAudit create(String companyId, CreateAuditRequest request, String performedBy) {
        SecurityAudit audit = new SecurityAudit();
        audit.setId(UUID.randomUUID().toString());
        audit.setCompanyId(companyId);
        audit.setTitle(request.getTitle());
        audit.setAuditType(request.getAuditType());
        audit.setStatus("SCOPING");
        audit.setScope(request.getScope());
        audit.setMethodology(request.getMethodology());
        audit.setLeadAuditor(request.getLeadAuditor());
        audit.setStartDate(request.getStartDate());
        audit.setEndDate(request.getEndDate());
        audit.setCreatedAt(LocalDateTime.now());
        audit.setUpdatedAt(LocalDateTime.now());

        audit = auditRepository.save(audit);

        activityLogService.log(companyId, "AUDIT", audit.getId(), "CREATED", performedBy,
                "Audit created: " + audit.getTitle());

        return audit;
    }

    @Transactional
    public SecurityAudit updateStatus(String auditId, String companyId, UpdateAuditStatusRequest request, String performedBy) {
        SecurityAudit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new RuntimeException("Audit not found"));
        if (!audit.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Access denied");
        }

        String newStatus = request.getStatus();
        validateStatusTransition(audit.getStatus(), newStatus);

        String oldStatus = audit.getStatus();
        audit.setStatus(newStatus);
        if (request.getExecutiveSummary() != null) {
            audit.setExecutiveSummary(request.getExecutiveSummary());
        }
        audit.setUpdatedAt(LocalDateTime.now());

        audit = auditRepository.save(audit);

        activityLogService.log(companyId, "AUDIT", audit.getId(), "STATUS_CHANGED", performedBy,
                "Status changed from " + oldStatus + " to " + newStatus);

        return audit;
    }

    @Transactional
    public AuditFinding addFinding(String auditId, String companyId, CreateFindingRequest request, String performedBy) {
        SecurityAudit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new RuntimeException("Audit not found"));
        if (!audit.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Access denied");
        }

        AuditFinding finding = new AuditFinding();
        finding.setId(UUID.randomUUID().toString());
        finding.setAuditId(auditId);
        finding.setTitle(request.getTitle());
        finding.setDescription(request.getDescription());
        finding.setRiskLevel(request.getRiskLevel());
        finding.setCvssScore(request.getCvssScore());
        finding.setAffectedAsset(request.getAffectedAsset());
        finding.setRecommendation(request.getRecommendation());
        finding.setStatus("OPEN");
        finding.setCreatedAt(LocalDateTime.now());

        finding = findingRepository.save(finding);

        activityLogService.log(companyId, "AUDIT", auditId, "FINDING_ADDED", performedBy,
                "Finding added: " + finding.getTitle() + " [" + finding.getRiskLevel() + "]");

        return finding;
    }

    @Transactional
    public AuditFinding updateFindingStatus(String findingId, String companyId, String newStatus, String performedBy) {
        AuditFinding finding = findingRepository.findById(findingId)
                .orElseThrow(() -> new RuntimeException("Finding not found"));

        SecurityAudit audit = auditRepository.findById(finding.getAuditId())
                .orElseThrow(() -> new RuntimeException("Audit not found"));
        if (!audit.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Access denied");
        }

        finding.setStatus(newStatus);
        finding = findingRepository.save(finding);

        activityLogService.log(companyId, "AUDIT_FINDING", findingId, "STATUS_CHANGED", performedBy,
                "Finding status changed to " + newStatus);

        return finding;
    }

    private void validateStatusTransition(String current, String next) {
        int currentIdx = AUDIT_LIFECYCLE.indexOf(current);
        int nextIdx = AUDIT_LIFECYCLE.indexOf(next);
        if (currentIdx == -1 || nextIdx == -1 || nextIdx != currentIdx + 1) {
            throw new RuntimeException("Cannot transition from " + current + " to " + next);
        }
    }

    private FindingSummary toFindingSummary(AuditFinding f) {
        FindingSummary s = new FindingSummary();
        s.setId(f.getId());
        s.setTitle(f.getTitle());
        s.setRiskLevel(f.getRiskLevel());
        s.setCvssScore(f.getCvssScore());
        s.setAffectedAsset(f.getAffectedAsset());
        s.setStatus(f.getStatus());
        s.setCreatedAt(f.getCreatedAt());
        return s;
    }
}
