package com.blackwolf.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

public class SuperAdminDTOs {

    @Data
    public static class GlobalDashboard {
        private int totalCompanies;
        private long totalThreats;
        private long threatsToday;
        private int totalSensors;
        private int activeSensors;
        private int totalBlockedIPs;
        private List<CompanySummary> companies;
        private Map<String, Long> globalAttackDistribution;
    }

    @Data
    public static class CompanySummary {
        private String id;
        private String companyName;
        private String domain;
        private String plan;
        private String status;
        private long threatCount;
        private int sensorCount;
    }
}
