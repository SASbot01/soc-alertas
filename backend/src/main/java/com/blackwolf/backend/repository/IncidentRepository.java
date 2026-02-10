package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, String> {
    List<Incident> findByCompanyId(String companyId);
    List<Incident> findByCompanyIdAndStatus(String companyId, String status);
    long countByCompanyId(String companyId);
    long countByCompanyIdAndStatus(String companyId, String status);
}
