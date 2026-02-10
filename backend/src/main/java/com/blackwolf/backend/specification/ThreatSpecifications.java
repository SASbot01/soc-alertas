package com.blackwolf.backend.specification;

import com.blackwolf.backend.model.ThreatEvent;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ThreatSpecifications {

    public static Specification<ThreatEvent> hasCompanyId(String companyId) {
        return (root, query, cb) -> cb.equal(root.get("companyId"), companyId);
    }

    public static Specification<ThreatEvent> hasThreatType(String threatType) {
        return (root, query, cb) -> cb.equal(root.get("threatType"), threatType);
    }

    public static Specification<ThreatEvent> hasStatus(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<ThreatEvent> hasSeverityGreaterThanOrEqual(Integer minSeverity) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("severity"), minSeverity);
    }

    public static Specification<ThreatEvent> hasSeverityLessThanOrEqual(Integer maxSeverity) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("severity"), maxSeverity);
    }

    public static Specification<ThreatEvent> timestampAfter(LocalDateTime after) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), after);
    }

    public static Specification<ThreatEvent> timestampBefore(LocalDateTime before) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), before);
    }

    public static Specification<ThreatEvent> searchByIp(String search) {
        return (root, query, cb) -> cb.or(
                cb.like(root.get("srcIp"), "%" + search + "%"),
                cb.like(root.get("dstIp"), "%" + search + "%")
        );
    }
}
