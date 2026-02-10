package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "threat_events")
public class ThreatEvent {
    @Id
    private String id;

    private String companyId;
    private String sensorId;
    private String threatType;
    private Integer severity;
    private String srcIp;
    private String dstIp;
    private Integer dstPort;
    private LocalDateTime timestamp;
    private String status;
    private String description;
}
