package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.AlertConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertConfigurationRepository extends JpaRepository<AlertConfiguration, String> {
    List<AlertConfiguration> findByCompanyId(String companyId);
    List<AlertConfiguration> findByCompanyIdAndIsActiveTrue(String companyId);
}
