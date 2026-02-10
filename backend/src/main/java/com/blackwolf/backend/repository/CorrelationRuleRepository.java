package com.blackwolf.backend.repository;

import com.blackwolf.backend.model.CorrelationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CorrelationRuleRepository extends JpaRepository<CorrelationRule, String> {
    List<CorrelationRule> findByIsActiveTrue();
}
