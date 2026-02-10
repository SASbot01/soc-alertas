package com.blackwolf.backend.service;

import com.blackwolf.backend.dto.DashboardDTOs.DashboardOverview;
import com.blackwolf.backend.dto.SuperAdminDTOs.*;
import com.blackwolf.backend.model.Company;
import com.blackwolf.backend.model.Sensor;
import com.blackwolf.backend.model.ThreatEvent;
import com.blackwolf.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SuperAdminService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ThreatEventRepository threatEventRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private BlockedIPRepository blockedIPRepository;

    @Autowired
    private DashboardService dashboardService;

    public GlobalDashboard getGlobalDashboard() {
        List<Company> companies = companyRepository.findAll();
        List<Sensor> allSensors = sensorRepository.findAll();
        List<ThreatEvent> allThreats = threatEventRepository.findAll();

        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();

        GlobalDashboard dashboard = new GlobalDashboard();
        dashboard.setTotalCompanies(companies.size());
        dashboard.setTotalThreats(allThreats.size());
        dashboard.setThreatsToday(allThreats.stream()
                .filter(t -> t.getTimestamp() != null && t.getTimestamp().isAfter(todayStart))
                .count());
        dashboard.setTotalSensors(allSensors.size());
        dashboard.setActiveSensors((int) allSensors.stream()
                .filter(s -> "online".equals(s.getStatus()))
                .count());
        dashboard.setTotalBlockedIPs((int) blockedIPRepository.count());

        // Per-company summaries
        List<CompanySummary> summaries = companies.stream().map(c -> {
            CompanySummary s = new CompanySummary();
            s.setId(c.getId());
            s.setCompanyName(c.getCompanyName());
            s.setDomain(c.getDomain());
            s.setPlan(c.getPlan());
            s.setStatus(c.getStatus());
            s.setThreatCount(threatEventRepository.countByCompanyId(c.getId()));
            s.setSensorCount(sensorRepository.findByCompanyId(c.getId()).size());
            return s;
        }).collect(Collectors.toList());
        dashboard.setCompanies(summaries);

        // Global attack distribution
        Map<String, Long> distribution = allThreats.stream()
                .collect(Collectors.groupingBy(ThreatEvent::getThreatType, Collectors.counting()));
        dashboard.setGlobalAttackDistribution(distribution);

        return dashboard;
    }

    public List<CompanySummary> getAllCompanies() {
        return companyRepository.findAll().stream().map(c -> {
            CompanySummary s = new CompanySummary();
            s.setId(c.getId());
            s.setCompanyName(c.getCompanyName());
            s.setDomain(c.getDomain());
            s.setPlan(c.getPlan());
            s.setStatus(c.getStatus());
            s.setThreatCount(threatEventRepository.countByCompanyId(c.getId()));
            s.setSensorCount(sensorRepository.findByCompanyId(c.getId()).size());
            return s;
        }).collect(Collectors.toList());
    }

    public DashboardOverview getCompanyDashboard(String companyId) {
        return dashboardService.getOverviewForCompany(companyId);
    }
}
