package com.blackwolf.backend.service;

import com.blackwolf.backend.model.CorrelationRule;
import com.blackwolf.backend.model.ThreatEvent;
import com.blackwolf.backend.repository.CorrelationRuleRepository;
import com.blackwolf.backend.repository.ThreatEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CorrelationService {

    private static final Logger log = LoggerFactory.getLogger(CorrelationService.class);

    @Autowired
    private CorrelationRuleRepository ruleRepository;

    @Autowired
    private ThreatEventRepository threatEventRepository;

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private AlertService alertService;

    public void evaluateThreat(ThreatEvent threat) {
        List<CorrelationRule> rules = ruleRepository.findByIsActiveTrue();

        for (CorrelationRule rule : rules) {
            if (matchesRule(threat, rule)) {
                log.info("Correlation rule '{}' triggered for threat {}", rule.getName(), threat.getId());

                if (rule.isCreatesIncident()) {
                    var incident = incidentService.createFromThreat(
                            threat.getCompanyId(),
                            threat.getId(),
                            threat.getThreatType(),
                            threat.getSeverity(),
                            "Auto-created by correlation rule: " + rule.getName() + "\n" + threat.getDescription()
                    );
                    alertService.fireAlertsForIncident(
                            threat.getCompanyId(), incident.getId(),
                            incident.getTitle(), incident.getSeverity());
                }
            }
        }
    }

    private boolean matchesRule(ThreatEvent threat, CorrelationRule rule) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(rule.getTimeWindowMinutes());
        List<ThreatEvent> recentThreats = threatEventRepository.findByCompanyId(threat.getCompanyId())
                .stream()
                .filter(t -> t.getTimestamp() != null && t.getTimestamp().isAfter(windowStart))
                .collect(Collectors.toList());

        return switch (rule.getRuleType()) {
            case "severity_threshold" ->
                    threat.getSeverity() != null && threat.getSeverity() >= rule.getMinSeverity();

            case "same_ip_same_type" -> {
                if (rule.getThreatType() != null && !rule.getThreatType().equals(threat.getThreatType())) {
                    yield false;
                }
                long count = recentThreats.stream()
                        .filter(t -> threat.getSrcIp().equals(t.getSrcIp())
                                && threat.getThreatType().equals(t.getThreatType()))
                        .count();
                yield count >= rule.getThresholdCount();
            }

            case "same_type_threshold" -> {
                if (rule.getThreatType() != null && !rule.getThreatType().equals(threat.getThreatType())) {
                    yield false;
                }
                long count = recentThreats.stream()
                        .filter(t -> threat.getThreatType().equals(t.getThreatType()))
                        .count();
                yield count >= rule.getThresholdCount();
            }

            case "same_ip_multi_type" -> {
                long distinctTypes = recentThreats.stream()
                        .filter(t -> threat.getSrcIp().equals(t.getSrcIp()))
                        .map(ThreatEvent::getThreatType)
                        .distinct()
                        .count();
                yield distinctTypes >= rule.getThresholdCount();
            }

            default -> false;
        };
    }
}
