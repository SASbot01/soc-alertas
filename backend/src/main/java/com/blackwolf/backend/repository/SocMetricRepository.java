package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.SocMetric;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SocMetricRepository extends JpaRepository<SocMetric, String> {
    List<SocMetric> findByCompanyId(String companyId);

    List<SocMetric> findByCompanyIdAndMetricName(String companyId, String metricName);

    List<SocMetric> findByCompanyIdAndPeriod(String companyId, String period);
}
