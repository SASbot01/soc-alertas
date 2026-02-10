package com.blackwolf.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class ThreatDTOs {

    @Data
    public static class ThreatFilterRequest {
        private String threatType;
        private String status;
        private Integer minSeverity;
        private Integer maxSeverity;
        private LocalDateTime from;
        private LocalDateTime to;
        private String search;
        private int page = 0;
        private int size = 20;
        private String sortBy = "timestamp";
        private String sortDir = "desc";
    }

    @Data
    public static class ThreatListResponse {
        private List<ThreatSummary> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    @Data
    public static class ThreatSummary {
        private String id;
        private String threatType;
        private Integer severity;
        private String srcIp;
        private String dstIp;
        private Integer dstPort;
        private LocalDateTime timestamp;
        private String status;
        private String description;
        private String sensorId;
    }

    @Data
    public static class UpdateThreatStatusRequest {
        private String status;
    }
}
