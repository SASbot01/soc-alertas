package com.blackwolf.backend.service;

import com.blackwolf.backend.dto.DashboardDTOs.*;
import com.blackwolf.backend.model.*;
import com.blackwolf.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ThreatEventRepository threatEventRepository;

    @Autowired
    private BlockedIPRepository blockedIPRepository;

    public DashboardOverview getOverview(String userId) {
        // In a real app we'd look up the user to get the company ID.
        // Or assume the controller passes the company ID extracted from the token.
        // Let's assume passed ID is CompanyID for simplicity as we extracted user from
        // token -> company
        return getOverviewForCompany(userId);
    }

    public DashboardOverview getOverviewForCompany(String companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        List<Sensor> sensors = sensorRepository.findByCompanyId(companyId);
        long threatsToday = threatEventRepository.countByCompanyIdAndTimestampAfter(
                companyId, LocalDateTime.now().toLocalDate().atStartOfDay());
        long totalThreats = threatEventRepository.countByCompanyId(companyId);
        int blockedIPs = blockedIPRepository.findByCompanyId(companyId).size();

        // Distribution
        List<ThreatEvent> allThreats = threatEventRepository.findByCompanyId(companyId);
        Map<String, Long> distribution = allThreats.stream()
                .collect(Collectors.groupingBy(ThreatEvent::getThreatType, Collectors.counting()));

        // Recent
        List<ThreatEvent> recent = threatEventRepository.findByCompanyId(companyId,
                PageRequest.of(0, 10, Sort.by("timestamp").descending()));

        Stats stats = new Stats();
        stats.setTotal_threats(totalThreats);
        stats.setThreats_today(threatsToday); // Implement specific query if essential
        stats.setActive_sensors((int) sensors.stream().filter(s -> "online".equals(s.getStatus())).count());
        stats.setTotal_sensors(sensors.size());
        stats.setBlocked_ips(blockedIPs);

        DashboardOverview overview = new DashboardOverview();

        Map<String, Object> companyInfo = new HashMap<>();
        companyInfo.put("name", company.getCompanyName());
        companyInfo.put("domain", company.getDomain());
        companyInfo.put("plan", company.getPlan());

        overview.setCompany(companyInfo);
        overview.setStats(stats);
        overview.setAttack_distribution(distribution);
        overview.setRecent_threats(recent);
        overview.setTimestamp(LocalDateTime.now().toString());

        return overview;
    }
}
