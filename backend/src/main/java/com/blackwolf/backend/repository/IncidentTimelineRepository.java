package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.IncidentTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidentTimelineRepository extends JpaRepository<IncidentTimeline, String> {
    List<IncidentTimeline> findByIncidentIdOrderByCreatedAtDesc(String incidentId);
}
