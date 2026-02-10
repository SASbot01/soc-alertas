package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "blocked_ips")
@IdClass(BlockedIPId.class)
public class BlockedIP {
    @Id
    private String ip;

    @Id
    private String companyId;

    private String reason;
    private LocalDateTime blockedAt;
    private LocalDateTime expiresAt;
}
