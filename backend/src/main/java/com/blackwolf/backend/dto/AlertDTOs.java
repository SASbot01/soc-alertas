package com.blackwolf.backend.dto;

import lombok.Data;

public class AlertDTOs {

    @Data
    public static class CreateAlertConfigRequest {
        private String alertType;
        private String destination;
        private Integer minSeverity;
    }

    @Data
    public static class UpdateAlertConfigRequest {
        private String destination;
        private Integer minSeverity;
        private Boolean isActive;
    }
}
