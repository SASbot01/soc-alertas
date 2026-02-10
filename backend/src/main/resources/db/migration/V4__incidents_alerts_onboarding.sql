-- =============================================
-- V4: Incidents, Alerts, Onboarding, Threat Intel
-- =============================================

-- Onboarding requests (public form submissions)
CREATE TABLE onboarding_requests (
    id VARCHAR(255) PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    domain VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50),
    -- Legal
    accepts_terms BOOLEAN DEFAULT FALSE,
    accepts_dpa BOOLEAN DEFAULT FALSE,
    accepts_nda BOOLEAN DEFAULT FALSE,
    -- SOC Configuration
    monitor_network BOOLEAN DEFAULT FALSE,
    monitor_endpoints BOOLEAN DEFAULT FALSE,
    monitor_cloud BOOLEAN DEFAULT FALSE,
    monitor_email BOOLEAN DEFAULT FALSE,
    -- Infrastructure
    num_servers INT,
    num_endpoints INT,
    num_locations INT,
    current_security_tools TEXT,
    additional_notes TEXT,
    -- Preferred alert channels
    alert_email VARCHAR(255),
    alert_slack_webhook VARCHAR(500),
    preferred_sla VARCHAR(50),
    -- Status
    status VARCHAR(50) DEFAULT 'pending',
    reviewed_by VARCHAR(255),
    reviewed_at DATETIME,
    created_at DATETIME
);

-- Incidents (escalated from threats)
CREATE TABLE incidents (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    severity VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'open',
    assigned_to VARCHAR(255),
    source_threat_id VARCHAR(255),
    sla_deadline DATETIME,
    created_at DATETIME,
    updated_at DATETIME,
    resolved_at DATETIME,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE INDEX idx_incidents_company ON incidents(company_id);
CREATE INDEX idx_incidents_status ON incidents(status);

-- Incident timeline
CREATE TABLE incident_timeline (
    id VARCHAR(255) PRIMARY KEY,
    incident_id VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    description TEXT,
    performed_by VARCHAR(255),
    created_at DATETIME,
    FOREIGN KEY (incident_id) REFERENCES incidents(id)
);

-- Alert configurations per company
CREATE TABLE alert_configurations (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    destination VARCHAR(500) NOT NULL,
    min_severity INT DEFAULT 7,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE INDEX idx_alert_config_company ON alert_configurations(company_id);

-- Alert history
CREATE TABLE alert_history (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    alert_config_id VARCHAR(255),
    threat_event_id VARCHAR(255),
    incident_id VARCHAR(255),
    alert_type VARCHAR(50),
    destination VARCHAR(500),
    subject VARCHAR(500),
    message TEXT,
    status VARCHAR(50) DEFAULT 'sent',
    sent_at DATETIME,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- Threat enrichment cache
CREATE TABLE threat_enrichments (
    ip VARCHAR(50) PRIMARY KEY,
    abuse_confidence_score INT,
    country_code VARCHAR(10),
    isp VARCHAR(255),
    domain VARCHAR(255),
    is_tor BOOLEAN DEFAULT FALSE,
    is_vpn BOOLEAN DEFAULT FALSE,
    total_reports INT DEFAULT 0,
    last_reported_at DATETIME,
    enriched_at DATETIME
);

-- Correlation rules
CREATE TABLE correlation_rules (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    rule_type VARCHAR(50) NOT NULL,
    threshold_count INT DEFAULT 5,
    time_window_minutes INT DEFAULT 10,
    threat_type VARCHAR(100),
    min_severity INT DEFAULT 1,
    creates_incident BOOLEAN DEFAULT TRUE,
    incident_severity VARCHAR(50) DEFAULT 'high',
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME
);

-- Seed default correlation rules
INSERT INTO correlation_rules (id, name, description, rule_type, threshold_count, time_window_minutes, threat_type, min_severity, creates_incident, incident_severity, is_active, created_at)
VALUES
    ('rule-brute-force', 'Brute Force Detection', 'Detects multiple brute force attempts from same IP in short window', 'same_ip_same_type', 5, 10, 'Brute Force', 1, TRUE, 'high', TRUE, NOW()),
    ('rule-ddos', 'DDoS Detection', 'Detects DDoS attacks with high frequency from multiple sources', 'same_type_threshold', 10, 5, 'DDoS', 5, TRUE, 'critical', TRUE, NOW()),
    ('rule-multi-vector', 'Multi-Vector Attack', 'Detects multiple different attack types from same IP', 'same_ip_multi_type', 3, 15, NULL, 3, TRUE, 'critical', TRUE, NOW()),
    ('rule-critical-any', 'Critical Threat Auto-Incident', 'Any threat with severity >= 9 creates incident automatically', 'severity_threshold', 1, 1, NULL, 9, TRUE, 'critical', TRUE, NOW());
