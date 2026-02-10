package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.ThreatEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface ThreatEventRepository extends JpaRepository<ThreatEvent, String>, JpaSpecificationExecutor<ThreatEvent> {
    List<ThreatEvent> findByCompanyId(String companyId, Pageable pageable);

    List<ThreatEvent> findByCompanyId(String companyId);

    long countByCompanyId(String companyId);

    long countByCompanyIdAndTimestampAfter(String companyId, LocalDateTime timestamp);
}
