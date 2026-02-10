package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {
    List<ActivityLog> findByCompanyId(String companyId, Pageable pageable);

    List<ActivityLog> findByEntityTypeAndEntityId(String entityType, String entityId);

    List<ActivityLog> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);
}
