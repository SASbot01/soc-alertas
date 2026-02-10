package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.AuditFinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditFindingRepository extends JpaRepository<AuditFinding, String> {
    List<AuditFinding> findByAuditId(String auditId);

    long countByAuditIdAndStatus(String auditId, String status);
}
