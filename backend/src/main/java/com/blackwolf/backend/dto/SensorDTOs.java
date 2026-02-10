package com.blackwolf.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

public class SensorDTOs {

    @Data
    public static class SensorDataUpload {
        private String company_id;
        private String sensor_id;
        private String api_key;
        private String timestamp;
        private List<Map<String, Object>> packets;
        private List<ThreatInfo> threats;
        private Map<String, Object> stats;
    }

    @Data
    public static class ThreatInfo {
        private String threat_type;
        private Integer severity;
        private String src_ip;
        private String dst_ip;
        private Integer dst_port;
        private String description;
    }

    @Data
    public static class SensorResponse {
        private String status;
        private int processed_packets;
        private int processed_threats;
        private List<Command> commands;
    }

    @Data
    public static class Command {
        private String type;
        private String ip;
        private String reason;

        public Command(String type, String ip, String reason) {
            this.type = type;
            this.ip = ip;
            this.reason = reason;
        }
    }
}
