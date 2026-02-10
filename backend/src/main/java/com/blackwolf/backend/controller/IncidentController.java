package com.blackwolf.backend.controller;

import com.blackwolf.backend.dto.IncidentDTOs.*;
import com.blackwolf.backend.model.Incident;
import com.blackwolf.backend.model.IncidentTimeline;
import com.blackwolf.backend.service.IncidentService;
import com.blackwolf.backend.util.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    @Autowired private IncidentService incidentService;
    @Autowired private AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<List<Incident>> list(Authentication auth) {
        String companyId = authUtils.getCompanyId(auth);
        return ResponseEntity.ok(incidentService.listByCompany(companyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidentDetailResponse> detail(@PathVariable String id, Authentication auth) {
        String companyId = authUtils.getCompanyId(auth);
        return ResponseEntity.ok(incidentService.getDetail(id, companyId));
    }

    @PostMapping
    public ResponseEntity<Incident> create(@RequestBody CreateIncidentRequest request, Authentication auth) {
        String companyId = authUtils.getCompanyId(auth);
        return ResponseEntity.ok(incidentService.createIncident(companyId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Incident> update(@PathVariable String id, @RequestBody UpdateIncidentRequest request, Authentication auth) {
        String companyId = authUtils.getCompanyId(auth);
        return ResponseEntity.ok(incidentService.updateIncident(id, companyId, request));
    }

    @PostMapping("/{id}/timeline")
    public ResponseEntity<IncidentTimeline> addTimeline(@PathVariable String id, @RequestBody AddTimelineRequest request, Authentication auth) {
        String companyId = authUtils.getCompanyId(auth);
        String userId = authUtils.getUserId(auth);
        return ResponseEntity.ok(incidentService.addTimeline(id, companyId, request, userId));
    }
}
