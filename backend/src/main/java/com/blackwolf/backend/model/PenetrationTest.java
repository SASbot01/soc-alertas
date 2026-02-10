package com.blackwolf.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "penetration_tests")
public class PenetrationTest {
    @Id
    private String id;

    private String companyId;
    private String title;
    private String testType;
    private String status;

    @Column(columnDefinition = "TEXT")
    private String scope;

    @Column(columnDefinition = "TEXT")
    private String rulesOfEngagement;

    @Column(columnDefinition = "TEXT")
    private String targetSystems;

    private String tester;
    private LocalDate startDate;
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String executiveSummary;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
