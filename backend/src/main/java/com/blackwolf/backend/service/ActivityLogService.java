package com.blackwolf.backend.service;

import com.blackwolf.backend.model.ActivityLog;
import com.blackwolf.backend.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    public void log(String companyId, String entityType, String entityId, String action, String performedBy, String details) {
        ActivityLog log = new ActivityLog();
        log.setId(UUID.randomUUID().toString());
        log.setCompanyId(companyId);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setDetails(details);
        log.setCreatedAt(LocalDateTime.now());
        activityLogRepository.save(log);
    }

    public List<ActivityLog> getByEntity(String entityType, String entityId) {
        return activityLogRepository.findByEntityTypeAndEntityId(entityType, entityId,
                PageRequest.of(0, 50, Sort.by("createdAt").descending()));
    }

    public List<ActivityLog> getByCompany(String companyId) {
        return activityLogRepository.findByCompanyId(companyId,
                PageRequest.of(0, 100, Sort.by("createdAt").descending()));
    }
}
