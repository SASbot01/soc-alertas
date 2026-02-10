-- =====================================================
-- V2: SOC Nucleus Tables (Modulo 2.3)
-- Security Audits, Pentesting, Vulnerabilities,
-- Certifications, SOC Metrics, Activity Log
-- =====================================================

-- Security Audits
CREATE TABLE security_audits (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    title VARCHAR(500) NOT NULL,
    audit_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCOPING',
    scope TEXT,
    methodology TEXT,
    lead_auditor VARCHAR(255),
    start_date DATE,
    end_date DATE,
    executive_summary TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Audit Findings
CREATE TABLE audit_findings (
    id VARCHAR(255) PRIMARY KEY,
    audit_id VARCHAR(255) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    risk_level VARCHAR(50) NOT NULL,
    cvss_score DECIMAL(3,1),
    affected_asset VARCHAR(500),
    recommendation TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL,
    FOREIGN KEY (audit_id) REFERENCES security_audits(id)
);

-- Penetration Tests
CREATE TABLE penetration_tests (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    title VARCHAR(500) NOT NULL,
    test_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNING',
    scope TEXT,
    rules_of_engagement TEXT,
    target_systems TEXT,
    tester VARCHAR(255),
    start_date DATE,
    end_date DATE,
    executive_summary TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Pentest Findings
CREATE TABLE pentest_findings (
    id VARCHAR(255) PRIMARY KEY,
    pentest_id VARCHAR(255) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    severity VARCHAR(50) NOT NULL,
    cvss_score DECIMAL(3,1),
    affected_component VARCHAR(500),
    proof_of_concept TEXT,
    evidence TEXT,
    recommendation TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL,
    FOREIGN KEY (pentest_id) REFERENCES penetration_tests(id)
);

-- Vulnerabilities
CREATE TABLE vulnerabilities (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    cve_id VARCHAR(50),
    risk_level VARCHAR(50) NOT NULL,
    cvss_score DECIMAL(3,1),
    affected_asset VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'DETECTED',
    remediation_plan TEXT,
    remediation_deadline DATE,
    verified_at DATETIME,
    detected_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Security Certifications
CREATE TABLE security_certifications (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    certification_type VARCHAR(100) NOT NULL,
    title VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    issued_at DATETIME,
    expires_at DATETIME,
    audit_id VARCHAR(255),
    issued_by VARCHAR(255),
    notes TEXT,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies(id),
    FOREIGN KEY (audit_id) REFERENCES security_audits(id)
);

-- SOC Metrics
CREATE TABLE soc_metrics (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    metric_name VARCHAR(255) NOT NULL,
    metric_value DECIMAL(10,2) NOT NULL,
    metric_unit VARCHAR(50),
    target_value DECIMAL(10,2),
    period VARCHAR(50) NOT NULL,
    recorded_at DATETIME NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Activity Log
CREATE TABLE activity_log (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    performed_by VARCHAR(255),
    details TEXT,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Indexes for performance
CREATE INDEX idx_security_audits_company ON security_audits(company_id);
CREATE INDEX idx_security_audits_status ON security_audits(status);
CREATE INDEX idx_audit_findings_audit ON audit_findings(audit_id);
CREATE INDEX idx_penetration_tests_company ON penetration_tests(company_id);
CREATE INDEX idx_penetration_tests_status ON penetration_tests(status);
CREATE INDEX idx_pentest_findings_pentest ON pentest_findings(pentest_id);
CREATE INDEX idx_vulnerabilities_company ON vulnerabilities(company_id);
CREATE INDEX idx_vulnerabilities_status ON vulnerabilities(status);
CREATE INDEX idx_security_certifications_company ON security_certifications(company_id);
CREATE INDEX idx_soc_metrics_company ON soc_metrics(company_id);
CREATE INDEX idx_soc_metrics_name ON soc_metrics(metric_name);
CREATE INDEX idx_activity_log_company ON activity_log(company_id);
CREATE INDEX idx_activity_log_entity ON activity_log(entity_type, entity_id);
