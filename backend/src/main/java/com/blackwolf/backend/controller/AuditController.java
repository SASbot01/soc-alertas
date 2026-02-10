package com.blackwolf.backend.controller;

import com.blackwolf.backend.dto.AuditDTOs.*;
import com.blackwolf.backend.model.ActivityLog;
import com.blackwolf.backend.model.AuditFinding;
import com.blackwolf.backend.model.SecurityAudit;
import com.blackwolf.backend.service.ActivityLogService;
import com.blackwolf.backend.service.AuditService;
import com.blackwolf.backend.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/audits")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<List<SecurityAudit>> list(Authentication auth) {
        return ResponseEntity.ok(auditService.listByCompany(authUtils.getCompanyId(auth)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditDetailResponse> getDetail(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(auditService.getDetail(id, authUtils.getCompanyId(auth)));
    }

    @PostMapping
    public ResponseEntity<SecurityAudit> create(Authentication auth, @RequestBody CreateAuditRequest request) {
        String companyId = authUtils.getCompanyId(auth);
        String performedBy = authUtils.getUser(auth).getEmail();
        return ResponseEntity.ok(auditService.create(companyId, request, performedBy));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SecurityAudit> updateStatus(Authentication auth, @PathVariable String id,
                                                       @RequestBody UpdateAuditStatusRequest request) {
        String companyId = authUtils.getCompanyId(auth);
        String performedBy = authUtils.getUser(auth).getEmail();
        return ResponseEntity.ok(auditService.updateStatus(id, companyId, request, performedBy));
    }

    @PostMapping("/{id}/findings")
    public ResponseEntity<AuditFinding> addFinding(Authentication auth, @PathVariable String id,
                                                    @RequestBody CreateFindingRequest request) {
        String companyId = authUtils.getCompanyId(auth);
        String performedBy = authUtils.getUser(auth).getEmail();
        return ResponseEntity.ok(auditService.addFinding(id, companyId, request, performedBy));
    }

    @PatchMapping("/findings/{findingId}/status")
    public ResponseEntity<AuditFinding> updateFindingStatus(Authentication auth,
                                                             @PathVariable String findingId,
                                                             @RequestBody Map<String, String> body) {
        String companyId = authUtils.getCompanyId(auth);
        String performedBy = authUtils.getUser(auth).getEmail();
        return ResponseEntity.ok(auditService.updateFindingStatus(findingId, companyId, body.get("status"), performedBy));
    }

    @GetMapping("/{id}/activity")
    public ResponseEntity<List<ActivityLog>> getActivity(@PathVariable String id) {
        return ResponseEntity.ok(activityLogService.getByEntity("AUDIT", id));
    }
}
