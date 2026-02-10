package com.blackwolf.backend.dto;

import com.blackwolf.backend.model.ThreatEvent;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class DashboardDTOs {

    @Data
    public static class DashboardOverview {
        private Map<String, Object> company;
        private Stats stats;
        private Map<String, Long> attack_distribution;
        private List<ThreatEvent> recent_threats;
        private String timestamp;
    }

    @Data
    public static class Stats {
        private Long total_threats;
        private Long threats_today;
        private int active_sensors;
        private int total_sensors;
        private int blocked_ips;
    }
}
