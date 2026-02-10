CREATE TABLE companies (
    id VARCHAR(255) PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    domain VARCHAR(255) NOT NULL UNIQUE,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50),
    plan VARCHAR(50) DEFAULT 'starter',
    api_key VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) DEFAULT 'active',
    registered_at DATETIME,
    total_threats BIGINT DEFAULT 0
);

CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'user',
    company_id VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME,
    last_login DATETIME,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE sensors (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    company_id VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    registered_at DATETIME,
    last_seen DATETIME,
    packets_processed BIGINT DEFAULT 0,
    threats_detected BIGINT DEFAULT 0,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE threat_events (
    id VARCHAR(255) PRIMARY KEY,
    company_id VARCHAR(255) NOT NULL,
    sensor_id VARCHAR(255),
    threat_type VARCHAR(100),
    severity INT,
    src_ip VARCHAR(50),
    dst_ip VARCHAR(50),
    dst_port INT,
    timestamp DATETIME,
    status VARCHAR(50),
    description TEXT,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE TABLE blocked_ips (
    ip VARCHAR(50) NOT NULL,
    company_id VARCHAR(255) NOT NULL,
    reason VARCHAR(255),
    blocked_at DATETIME,
    expires_at DATETIME,
    PRIMARY KEY (ip, company_id),
    FOREIGN KEY (company_id) REFERENCES companies(id)
);
