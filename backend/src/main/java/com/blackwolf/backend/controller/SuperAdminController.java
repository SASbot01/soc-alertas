package com.blackwolf.backend.controller;

import com.blackwolf.backend.dto.DashboardDTOs.DashboardOverview;
import com.blackwolf.backend.dto.SuperAdminDTOs.*;
import com.blackwolf.backend.service.SuperAdminService;
import com.blackwolf.backend.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/superadmin")
public class SuperAdminController {

    @Autowired
    private SuperAdminService superAdminService;

    @Autowired
    private AuthUtils authUtils;

    private void requireSuperAdmin(Authentication authentication) {
        if (!authUtils.isSuperAdmin(authentication)) {
            throw new RuntimeException("Access denied: superadmin role required");
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<GlobalDashboard> getGlobalDashboard(Authentication authentication) {
        requireSuperAdmin(authentication);
        return ResponseEntity.ok(superAdminService.getGlobalDashboard());
    }

    @GetMapping("/companies")
    public ResponseEntity<List<CompanySummary>> getAllCompanies(Authentication authentication) {
        requireSuperAdmin(authentication);
        return ResponseEntity.ok(superAdminService.getAllCompanies());
    }

    @GetMapping("/companies/{id}/dashboard")
    public ResponseEntity<DashboardOverview> getCompanyDashboard(Authentication authentication, @PathVariable String id) {
        requireSuperAdmin(authentication);
        return ResponseEntity.ok(superAdminService.getCompanyDashboard(id));
    }
}
