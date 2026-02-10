package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.ThreatEnrichment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThreatEnrichmentRepository extends JpaRepository<ThreatEnrichment, String> {
}
