package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sensors")
public class Sensor {
    @Id
    private String id;

    private String name;
    private String companyId;
    private String status;
    private LocalDateTime registeredAt;
    private LocalDateTime lastSeen;
    private Long packetsProcessed;
    private Long threatsDetected;
}
