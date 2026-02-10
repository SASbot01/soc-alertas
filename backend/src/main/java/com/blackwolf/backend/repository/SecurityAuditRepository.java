package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.SecurityAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityAuditRepository extends JpaRepository<SecurityAudit, String> {
    List<SecurityAudit> findByCompanyId(String companyId);

    List<SecurityAudit> findByCompanyIdAndStatus(String companyId, String status);

    long countByCompanyIdAndStatus(String companyId, String status);
}
