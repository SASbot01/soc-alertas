-- =============================================
-- V5: Seed Demo Data for SOC Metrics Dashboard
-- Uses existing company: BlackWolf Security
-- =============================================

SET @cid = '18036617-5eb6-460c-b217-9c4b0e984a5e';

-- ===== Security Audits =====
INSERT INTO security_audits (id, company_id, title, audit_type, status, scope, methodology, lead_auditor, start_date, end_date, executive_summary, created_at, updated_at) VALUES
('audit-seed-001', @cid, 'Infrastructure Security Audit Q1', 'INFRASTRUCTURE', 'DELIVERED', 'Full network infrastructure review', 'NIST CSF + CIS Controls', 'Carlos Ruiz', '2025-11-01', '2025-12-15', 'Audit completed with 3 critical findings resolved', NOW(), NOW()),
('audit-seed-002', @cid, 'Web Application Security Audit', 'APPLICATION', 'DELIVERED', 'Main web application and APIs', 'OWASP Top 10 + ASVS', 'Maria Lopez', '2025-12-01', '2026-01-10', 'Application hardened, 2 high-risk issues mitigated', NOW(), NOW()),
('audit-seed-003', @cid, 'Cloud Security Assessment', 'CLOUD', 'REPORTING', 'AWS infrastructure and IAM policies', 'CIS AWS Benchmark', 'David Garcia', '2026-01-15', '2026-02-15', NULL, NOW(), NOW()),
('audit-seed-004', @cid, 'Compliance Audit ISO 27001', 'COMPLIANCE', 'TESTING', 'ISMS scope - all departments', 'ISO 27001:2022', 'Ana Martinez', '2026-01-20', NULL, NULL, NOW(), NOW()),
('audit-seed-005', @cid, 'Network Segmentation Review', 'INFRASTRUCTURE', 'SCOPING', 'Internal network segmentation', 'NIST SP 800-41', 'Carlos Ruiz', NULL, NULL, NULL, NOW(), NOW());

-- Audit Findings
INSERT INTO audit_findings (id, audit_id, title, description, risk_level, cvss_score, affected_asset, recommendation, status, created_at) VALUES
('af-seed-001', 'audit-seed-001', 'Unpatched critical vulnerability in firewall', 'FortiGate firmware outdated with known CVE', 'CRITICAL', 9.8, 'Firewall FW-01', 'Update firmware immediately', 'RESOLVED', NOW()),
('af-seed-002', 'audit-seed-001', 'Weak SSH configuration on servers', 'Password authentication enabled, no key-only', 'HIGH', 7.5, 'Linux Servers', 'Enforce key-based SSH only', 'RESOLVED', NOW()),
('af-seed-003', 'audit-seed-001', 'Missing network segmentation', 'Flat network between DMZ and internal', 'HIGH', 8.1, 'Network Infrastructure', 'Implement VLANs and ACLs', 'OPEN', NOW()),
('af-seed-004', 'audit-seed-002', 'SQL Injection in search endpoint', 'Parameterized queries not used in /api/search', 'CRITICAL', 9.1, 'Web Application API', 'Use parameterized queries', 'RESOLVED', NOW()),
('af-seed-005', 'audit-seed-002', 'Missing rate limiting on auth endpoints', 'No brute force protection on /login', 'HIGH', 7.3, 'Authentication Module', 'Implement rate limiting', 'RESOLVED', NOW());

-- ===== Penetration Tests =====
INSERT INTO penetration_tests (id, company_id, title, test_type, status, scope, rules_of_engagement, target_systems, tester, start_date, end_date, executive_summary, created_at, updated_at) VALUES
('pentest-seed-001', @cid, 'External Pentest Q4 2025', 'BLACK_BOX', 'DELIVERED', 'All external-facing services', 'No DoS, business hours only', 'web.blackwolfsec.io, api.blackwolfsec.io', 'Pedro Sanchez', '2025-10-15', '2025-11-30', 'Identified 5 vulnerabilities, 3 critical patched', NOW(), NOW()),
('pentest-seed-002', @cid, 'Internal Network Pentest', 'GREY_BOX', 'DELIVERED', 'Internal corporate network', 'Standard ROE, no production impact', '10.0.0.0/8 internal range', 'Laura Fernandez', '2025-12-01', '2026-01-15', 'Lateral movement possible via shared credentials', NOW(), NOW()),
('pentest-seed-003', @cid, 'API Security Testing', 'WHITE_BOX', 'REPORTING', 'REST API v1 endpoints', 'Full source access, staging env', 'api.blackwolfsec.io/v1/*', 'Pedro Sanchez', '2026-01-20', '2026-02-10', NULL, NOW(), NOW()),
('pentest-seed-004', @cid, 'Mobile App Security Test', 'GREY_BOX', 'EXPLOITATION', 'iOS and Android apps', 'Test accounts provided', 'BlackWolf Mobile App v2.1', 'Laura Fernandez', '2026-02-01', NULL, NULL, NOW(), NOW());

-- Pentest Findings
INSERT INTO pentest_findings (id, pentest_id, title, description, severity, cvss_score, affected_component, proof_of_concept, evidence, recommendation, status, created_at) VALUES
('pf-seed-001', 'pentest-seed-001', 'RCE via deserialization', 'Java deserialization vulnerability in API gateway', 'CRITICAL', 9.8, 'API Gateway', 'ysoserial payload executed', 'Screenshot of reverse shell', 'Upgrade library, input validation', 'RESOLVED', NOW()),
('pf-seed-002', 'pentest-seed-001', 'SSRF in file upload', 'Server-side request forgery via image URL parameter', 'HIGH', 8.2, 'File Upload Service', 'Internal metadata accessed via SSRF', 'HTTP response with internal data', 'Whitelist allowed URLs', 'RESOLVED', NOW()),
('pf-seed-003', 'pentest-seed-002', 'Pass-the-hash lateral movement', 'NTLM hashes reusable across workstations', 'CRITICAL', 9.0, 'Active Directory', 'PtH attack from WS-01 to DC-01', 'Mimikatz output', 'Implement LAPS, disable NTLM', 'OPEN', NOW());

-- ===== Vulnerabilities =====
INSERT INTO vulnerabilities (id, company_id, title, description, cve_id, risk_level, cvss_score, affected_asset, status, remediation_plan, remediation_deadline, verified_at, detected_at, updated_at) VALUES
('vuln-seed-001', @cid, 'Log4Shell in internal app', 'Log4j RCE vulnerability CVE-2021-44228', 'CVE-2021-44228', 'CRITICAL', 10.0, 'Internal Java Application', 'VERIFIED', 'Upgraded to Log4j 2.17.1', '2025-10-01', NOW(), DATE_SUB(NOW(), INTERVAL 90 DAY), NOW()),
('vuln-seed-002', @cid, 'OpenSSL Buffer Overflow', 'OpenSSL vulnerability allowing DoS', 'CVE-2024-0727', 'HIGH', 7.5, 'Web Server nginx', 'FIXED', 'Updated OpenSSL to 3.2.1', '2025-11-15', NULL, DATE_SUB(NOW(), INTERVAL 60 DAY), NOW()),
('vuln-seed-003', @cid, 'XSS in customer portal', 'Stored XSS via user profile field', 'CVE-2025-1234', 'HIGH', 7.1, 'Customer Portal', 'FIXED', 'Input sanitization applied', '2025-12-01', NULL, DATE_SUB(NOW(), INTERVAL 45 DAY), NOW()),
('vuln-seed-004', @cid, 'Privilege escalation in CMS', 'Broken access control allows admin access', NULL, 'CRITICAL', 9.1, 'Content Management System', 'IN_REMEDIATION', 'Implementing RBAC fix', '2026-02-20', NULL, DATE_SUB(NOW(), INTERVAL 15 DAY), NOW()),
('vuln-seed-005', @cid, 'Weak TLS configuration', 'TLS 1.0/1.1 still enabled on endpoints', NULL, 'MEDIUM', 5.3, 'Load Balancer', 'CONFIRMED', 'Disable TLS < 1.2', '2026-03-01', NULL, DATE_SUB(NOW(), INTERVAL 10 DAY), NOW()),
('vuln-seed-006', @cid, 'Outdated WordPress plugins', 'Multiple plugins with known CVEs', NULL, 'HIGH', 7.8, 'Corporate Blog', 'DETECTED', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW()),
('vuln-seed-007', @cid, 'CSRF in admin panel', 'Missing CSRF tokens in state-changing requests', NULL, 'MEDIUM', 6.5, 'Admin Dashboard', 'IN_REMEDIATION', 'Adding CSRF middleware', '2026-02-28', NULL, DATE_SUB(NOW(), INTERVAL 20 DAY), NOW());

-- ===== Security Certifications =====
INSERT INTO security_certifications (id, company_id, certification_type, title, status, issued_at, expires_at, audit_id, issued_by, notes, created_at) VALUES
('cert-seed-001', @cid, 'ISO27001', 'ISO 27001:2022 Certification', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 6 MONTH), DATE_ADD(NOW(), INTERVAL 18 MONTH), 'audit-seed-001', 'BSI Group', 'Full ISMS certification', NOW()),
('cert-seed-002', @cid, 'SOC2', 'SOC 2 Type II Report', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 3 MONTH), DATE_ADD(NOW(), INTERVAL 9 MONTH), 'audit-seed-002', 'Deloitte', 'Trust Services Criteria met', NOW()),
('cert-seed-003', @cid, 'PCI_DSS', 'PCI DSS v4.0 Compliance', 'ACTIVE', DATE_SUB(NOW(), INTERVAL 2 MONTH), DATE_ADD(NOW(), INTERVAL 10 MONTH), NULL, 'QSA Certified', 'Level 1 merchant compliance', NOW());

-- ===== SOC Custom Metrics =====
INSERT INTO soc_metrics (id, company_id, metric_name, metric_value, metric_unit, target_value, period, recorded_at) VALUES
('metric-seed-001', @cid, 'Mean Time to Detect (MTTD)', 4.20, 'hours', 2.00, '2026-Q1', NOW()),
('metric-seed-002', @cid, 'Mean Time to Respond (MTTR)', 12.50, 'hours', 8.00, '2026-Q1', NOW()),
('metric-seed-003', @cid, 'Incident Resolution Rate', 94.30, '%', 99.00, '2026-Q1', NOW()),
('metric-seed-004', @cid, 'Threats Blocked', 1247.00, 'events', 0.00, '2026-Q1', NOW()),
('metric-seed-005', @cid, 'False Positive Rate', 8.70, '%', 5.00, '2026-Q1', NOW()),
('metric-seed-006', @cid, 'SLA Compliance', 97.20, '%', 99.50, '2026-Q1', NOW()),
('metric-seed-007', @cid, 'Vulnerabilities Patched', 23.00, 'count', 30.00, '2026-January', DATE_SUB(NOW(), INTERVAL 30 DAY)),
('metric-seed-008', @cid, 'Vulnerabilities Patched', 31.00, 'count', 30.00, '2026-February', NOW()),
('metric-seed-009', @cid, 'Security Score', 82.50, 'points', 90.00, '2026-Q1', NOW()),
('metric-seed-010', @cid, 'Phishing Emails Blocked', 342.00, 'emails', 0.00, '2026-January', DATE_SUB(NOW(), INTERVAL 30 DAY));
