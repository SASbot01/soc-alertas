package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.SecurityCertification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SecurityCertificationRepository extends JpaRepository<SecurityCertification, String> {
    List<SecurityCertification> findByCompanyId(String companyId);

    List<SecurityCertification> findByCompanyIdAndStatus(String companyId, String status);

    long countByCompanyIdAndExpiresAtAfter(String companyId, LocalDateTime now);
}
