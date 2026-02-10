package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "alert_history")
public class AlertHistory {
    @Id
    private String id;
    private String companyId;
    private String alertConfigId;
    private String threatEventId;
    private String incidentId;
    private String alertType;
    private String destination;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String status;
    private LocalDateTime sentAt;
}
