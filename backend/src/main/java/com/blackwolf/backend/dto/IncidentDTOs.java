package com.blackwolf.backend.dto;

import com.blackwolf.backend.model.Incident;
import com.blackwolf.backend.model.IncidentTimeline;
import lombok.Data;
import java.util.List;

public class IncidentDTOs {

    @Data
    public static class CreateIncidentRequest {
        private String title;
        private String description;
        private String severity;
        private String assignedTo;
        private String sourceThreatId;
    }

    @Data
    public static class UpdateIncidentRequest {
        private String status;
        private String assignedTo;
        private String description;
    }

    @Data
    public static class AddTimelineRequest {
        private String action;
        private String description;
    }

    @Data
    public static class IncidentDetailResponse {
        private Incident incident;
        private List<IncidentTimeline> timeline;
    }
}
